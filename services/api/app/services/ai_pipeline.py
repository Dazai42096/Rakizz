from pathlib import Path
import random
import re
from typing import Any

from fastapi import HTTPException
from pypdf import PdfReader

from shared_python.schemas.quiz import DifficultyLevel


class AIPipelineService:
    def generate_quiz(
        self,
        material_url: str,
        difficulty: DifficultyLevel,
        old_question_texts: list[str] | None = None,
        old_source_snippets: list[str] | None = None,
    ) -> list[dict[str, Any]]:
        # first thing: read the material
        text = self._read_material_text(material_url)

        if not text.strip():
            raise HTTPException(
                status_code=400,
                detail="Could not read text from this material",
            )

        old_question_texts = old_question_texts or []
        old_source_snippets = old_source_snippets or []

        # generate questions from the material chunks
        questions = self._build_questions_from_text(
            text=text,
            difficulty=difficulty,
            old_question_texts=old_question_texts,
            old_source_snippets=old_source_snippets,
        )

        if not questions:
            raise HTTPException(
                status_code=400,
                detail="Could not generate questions from this material",
            )

        return questions

    def _read_material_text(
        self,
        material_url: str,
    ) -> str:
        path = Path(material_url)

        if not path.exists():
            # fallback, just in case the material is saved as text/url
            return material_url

        suffix = path.suffix.lower()

        if suffix == ".pdf":
            return self._read_pdf(path)

        if suffix == ".txt":
            return path.read_text(
                encoding="utf-8",
                errors="ignore",
            )

        # basic fallback for checkpoint
        return path.name.replace("_", " ").replace("-", " ")

    def _read_pdf(
        self,
        path: Path,
    ) -> str:
        try:
            reader = PdfReader(str(path))
            pages_text = []

            for page in reader.pages:
                # take text from each page
                page_text = page.extract_text() or ""
                pages_text.append(page_text)

            return self._clean_text("\n".join(pages_text))
        except Exception as exc:
            raise HTTPException(
                status_code=400,
                detail=f"Could not read PDF text: {exc}",
            )

    def _build_questions_from_text(
        self,
        text: str,
        difficulty: DifficultyLevel,
        old_question_texts: list[str],
        old_source_snippets: list[str],
    ) -> list[dict[str, Any]]:
        clean_text = self._clean_text(text)

        # split the material so questions come from different places
        chunks = self._make_chunks(clean_text)

        if not chunks:
            chunks = [clean_text]

        # try to skip chunks already used in old quizzes
        fresh_chunks = self._remove_used_chunks(
            chunks=chunks,
            old_source_snippets=old_source_snippets,
        )

        if len(fresh_chunks) < 3:
            # if most chunks were used before, reuse but shuffle
            fresh_chunks = chunks

        random.shuffle(fresh_chunks)

        question_count = self._question_count(difficulty)

        # MIXED means easy + medium + hard in one quiz
        levels = self._difficulty_plan(
            difficulty=difficulty,
            count=question_count,
        )

        questions: list[dict[str, Any]] = []

        # first pass, take one question from different chunks
        for i in range(question_count):
            if not fresh_chunks:
                break

            chunk = fresh_chunks[i % len(fresh_chunks)]
            level = levels[i % len(levels)]

            q = self._make_question_from_chunk(
                chunk=chunk,
                level=level,
                question_number=i + 1,
            )

            if q is None:
                continue

            if self._was_used_before(q["question_text"], old_question_texts):
                continue

            if self._is_repeated_in_same_quiz(q, questions):
                continue

            questions.append(q)

        # second pass, fill missing questions if some chunks failed
        tries = 0

        while len(questions) < question_count and tries < 100:
            tries += 1

            chunk = random.choice(chunks)
            level = random.choice(levels)

            q = self._make_question_from_chunk(
                chunk=chunk,
                level=level,
                question_number=len(questions) + 1,
            )

            if q is None:
                continue

            if self._is_repeated_in_same_quiz(q, questions):
                continue

            questions.append(q)

        return questions[:question_count]

    def _make_chunks(
        self,
        text: str,
    ) -> list[str]:
        sentences = self._split_sentences(text)

        chunks = []
        current = []

        for sentence in sentences:
            current.append(sentence)

            joined = " ".join(current)

            # smaller chunks help cover more parts of the material
            if len(joined) >= 330:
                chunks.append(joined.strip())
                current = []

        if current:
            chunks.append(" ".join(current).strip())

        final_chunks = []

        for chunk in chunks:
            if len(chunk) >= 90:
                final_chunks.append(chunk)

        return final_chunks

    def _split_sentences(
        self,
        text: str,
    ) -> list[str]:
        parts = re.split(r"(?<=[.!?])\s+", text)

        good = []

        for part in parts:
            part = self._clean_text(part)

            if len(part) >= 30:
                good.append(part)

        return good

    def _make_question_from_chunk(
        self,
        chunk: str,
        level: DifficultyLevel,
        question_number: int,
    ) -> dict[str, Any] | None:
        sentences = self._split_sentences(chunk)

        if not sentences:
            return None

        main_sentence = self._pick_good_sentence(sentences)

        if not main_sentence:
            return None

        keyword = self._pick_keyword(main_sentence)

        if not keyword:
            return None

        correct_answer = self._short_answer(main_sentence)

        wrong_answers = self._make_wrong_answers(
            keyword=keyword,
            correct_answer=correct_answer,
        )

        if len(wrong_answers) < 3:
            return None

        options = [
            correct_answer,
            wrong_answers[0],
            wrong_answers[1],
            wrong_answers[2],
        ]

        random.shuffle(options)

        question_text, explanation = self._question_text_for_level(
            keyword=keyword,
            level=level,
            question_number=question_number,
        )

        return {
            "question_text": question_text,
            "options": options,
            "correct_answer": correct_answer,
            "explanation": explanation,
            "source_chunk_snippet": self._short_snippet(chunk),
        }

    def _question_text_for_level(
        self,
        keyword: str,
        level: DifficultyLevel,
        question_number: int,
    ) -> tuple[str, str]:
        # the level is shown inside the question so the review sees the mix

        if level == DifficultyLevel.EASY:
            return (
                f"[EASY Q{question_number}] According to the material, what is the correct idea about {keyword}?",
                "This is a direct question from the uploaded material.",
            )

        if level == DifficultyLevel.MEDIUM:
            return (
                f"[MEDIUM Q{question_number}] Which statement best explains {keyword} from the material?",
                "This question needs understanding the idea from the material.",
            )

        return (
            f"[HARD Q{question_number}] Which answer best applies the idea of {keyword}?",
            "This is harder because it asks the student to apply the idea, not only memorize it.",
        )

    def _pick_good_sentence(
        self,
        sentences: list[str],
    ) -> str | None:
        scored = []

        for sentence in sentences:
            score = 0
            lower = sentence.lower()

            good_words = [
                "is",
                "are",
                "means",
                "used",
                "helps",
                "allows",
                "because",
                "important",
                "refers",
                "includes",
                "provides",
                "difference",
                "example",
                "main",
                "called",
                "known",
            ]

            for word in good_words:
                if word in lower:
                    score += 1

            if 50 <= len(sentence) <= 190:
                score += 2

            if len(sentence) > 240:
                score -= 2

            scored.append((score, sentence))

        scored.sort(key=lambda item: item[0], reverse=True)

        if not scored:
            return None

        # choose one from the best few so it is not always the same
        best_few = scored[: min(5, len(scored))]

        return random.choice(best_few)[1]

    def _pick_keyword(
        self,
        sentence: str,
    ) -> str | None:
        words = re.findall(r"[A-Za-z][A-Za-z0-9\-]{3,}", sentence)

        bad_words = {
            "this",
            "that",
            "with",
            "from",
            "have",
            "will",
            "they",
            "their",
            "about",
            "which",
            "when",
            "where",
            "used",
            "uses",
            "using",
            "because",
            "there",
            "these",
            "those",
            "each",
            "every",
            "into",
            "only",
            "also",
            "more",
            "most",
            "some",
            "same",
            "such",
            "between",
            "student",
            "material",
            "question",
            "answer",
            "correct",
            "uploaded",
            "lesson",
            "study",
        }

        candidates = []

        for word in words:
            if word.lower() not in bad_words:
                candidates.append(word)

        if not candidates:
            return None

        candidates.sort(key=len, reverse=True)

        return candidates[0]

    def _make_wrong_answers(
        self,
        keyword: str,
        correct_answer: str,
    ) -> list[str]:
        # simple wrong options, enough for checkpoint preview
        wrongs = [
            f"{keyword} is not mentioned in the uploaded material.",
            f"{keyword} means the same thing as the student's password.",
            f"{keyword} is only used to change the profile picture.",
            f"{keyword} is a random app setting and not part of the lesson.",
            f"{keyword} means the student has already passed the quiz.",
            f"{keyword} is only related to phone notifications.",
            f"{keyword} is used only for signing out of Rakizz.",
            f"{keyword} is only a visual theme setting inside the app.",
            f"{keyword} is not connected to the topic explained in the file.",
            f"{keyword} is mainly used for deleting uploaded material.",
        ]

        final_wrong = []

        for item in wrongs:
            item = self._clean_text(item)

            if item != correct_answer and item not in final_wrong:
                final_wrong.append(item)

        random.shuffle(final_wrong)

        return final_wrong[:3]

    def _remove_used_chunks(
        self,
        chunks: list[str],
        old_source_snippets: list[str],
    ) -> list[str]:
        if not old_source_snippets:
            return chunks

        fresh = []

        for chunk in chunks:
            chunk_small = self._clean_text(chunk[:160]).lower()
            used = False

            for old in old_source_snippets:
                old_small = self._clean_text((old or "")[:120]).lower()

                if old_small and old_small in chunk_small:
                    used = True
                    break

            if not used:
                fresh.append(chunk)

        return fresh

    def _was_used_before(
        self,
        question_text: str,
        old_question_texts: list[str],
    ) -> bool:
        new_q = self._clean_text(question_text).lower()

        for old_q in old_question_texts:
            if self._clean_text(old_q).lower() == new_q:
                return True

        return False

    def _is_repeated_in_same_quiz(
        self,
        question: dict[str, Any],
        questions: list[dict[str, Any]],
    ) -> bool:
        for old in questions:
            if old["question_text"] == question["question_text"]:
                return True

            if old["source_chunk_snippet"] == question["source_chunk_snippet"]:
                return True

        return False

    def _question_count(
        self,
        difficulty: DifficultyLevel,
    ) -> int:
        # from now on the app uses MIXED
        # keeping the old ones in case old requests are sent from Swagger
        if difficulty == DifficultyLevel.EASY:
            return 10

        if difficulty == DifficultyLevel.MEDIUM:
            return 15

        return 20

    def _difficulty_plan(
        self,
        difficulty: DifficultyLevel,
        count: int,
    ) -> list[DifficultyLevel]:
        # mixed is the real checkpoint flow now
        if difficulty == DifficultyLevel.MIXED:
            plan = [
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.EASY,
                DifficultyLevel.HARD,
            ]

        elif difficulty == DifficultyLevel.EASY:
            plan = [
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
            ]

        elif difficulty == DifficultyLevel.MEDIUM:
            plan = [
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.HARD,
            ]

        else:
            plan = [
                DifficultyLevel.EASY,
                DifficultyLevel.EASY,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
                DifficultyLevel.HARD,
            ]

        while len(plan) < count:
            plan.append(DifficultyLevel.MIXED)

        return plan[:count]

    def _short_answer(
        self,
        text: str,
    ) -> str:
        text = self._clean_text(text)

        if len(text) <= 160:
            return text

        return text[:157].rstrip() + "..."

    def _short_snippet(
        self,
        text: str,
    ) -> str:
        text = self._clean_text(text)

        if len(text) <= 280:
            return text

        return text[:277].rstrip() + "..."

    def _clean_text(
        self,
        text: str,
    ) -> str:
        return re.sub(r"\s+", " ", text).strip()


ai_service = AIPipelineService()