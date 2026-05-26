from dataclasses import dataclass
from pathlib import Path
import random
import re
from typing import Any

from fastapi import HTTPException
from pypdf import PdfReader

from shared_python.schemas.quiz import DifficultyLevel


@dataclass
class ConceptCard:
    topic: str
    fact: str
    source_snippet: str
    category: str


class AIPipelineService:
    def generate_quiz(
        self,
        material_url: str,
        difficulty: DifficultyLevel,
        old_question_texts: list[str] | None = None,
        old_source_snippets: list[str] | None = None,
    ) -> list[dict[str, Any]]:
        text = self._read_material_text(material_url)

        if not text.strip():
            raise HTTPException(
                status_code=400,
                detail="Could not read text from this material",
            )

        clean_text = self._clean_text(text)
        clean_text = self._remove_review_section(clean_text)

        old_question_texts = old_question_texts or []
        old_source_snippets = old_source_snippets or []

        concept_cards = self._extract_concept_cards(clean_text)

        if not concept_cards:
            raise HTTPException(
                status_code=400,
                detail="Could not find enough useful study content in this material",
            )

        concept_cards = self._remove_used_cards(
            cards=concept_cards,
            old_source_snippets=old_source_snippets,
        ) or concept_cards

        question_count = self._question_count(difficulty)
        difficulty_plan = self._difficulty_plan(
            difficulty=difficulty,
            count=question_count,
        )

        questions: list[dict[str, Any]] = []
        shuffled_cards = concept_cards.copy()
        random.shuffle(shuffled_cards)

        attempts = 0
        max_attempts = question_count * 10

        while len(questions) < question_count and attempts < max_attempts:
            attempts += 1

            card = shuffled_cards[attempts % len(shuffled_cards)]
            level = difficulty_plan[len(questions) % len(difficulty_plan)]

            question = self._build_question(
                card=card,
                level=level,
                question_number=len(questions) + 1,
                all_cards=concept_cards,
            )

            if question is None:
                continue

            if self._was_used_before(question["question_text"], old_question_texts):
                continue

            if self._is_duplicate_question(question, questions):
                continue

            questions.append(question)

        if len(questions) < min(5, question_count):
            raise HTTPException(
                status_code=400,
                detail="Could not generate enough quality questions from this material",
            )

        return questions[:question_count]

    def _read_material_text(
        self,
        material_url: str,
    ) -> str:
        path = self._resolve_material_path(material_url)

        if path is None:
            if material_url.startswith("http://") or material_url.startswith("https://"):
                raise HTTPException(
                    status_code=400,
                    detail="URL-based material reading is not available yet. Upload a file instead.",
                )

            raise HTTPException(
                status_code=400,
                detail="Material file was not found. Re-upload the material and try again.",
            )

        suffix = path.suffix.lower()

        if suffix == ".pdf":
            return self._read_pdf(path)

        if suffix == ".txt":
            return path.read_text(
                encoding="utf-8",
                errors="ignore",
            )

        if suffix in {".doc", ".docx", ".ppt", ".pptx"}:
            return self._read_basic_binary_document(path)

        if suffix in {".jpg", ".jpeg", ".png"}:
            raise HTTPException(
                status_code=400,
                detail="Image material OCR is not available yet. Upload a text or PDF file.",
            )

        raise HTTPException(
            status_code=400,
            detail="Unsupported material format for quiz generation",
        )

    def _resolve_material_path(
        self,
        material_url: str,
    ) -> Path | None:
        if not material_url:
            return None

        clean_source = material_url.strip()

        if clean_source.startswith("http://") or clean_source.startswith("https://"):
            return None

        project_root = Path(__file__).resolve().parents[2]
        upload_dir = project_root / "uploads" / "materials"
        source_path = Path(clean_source)

        candidates: list[Path] = []

        if source_path.is_absolute():
            candidates.append(source_path)
            candidates.append(upload_dir / source_path.name)
        else:
            candidates.append(project_root / source_path)
            candidates.append(upload_dir / source_path.name)
            candidates.append(source_path)

        for candidate in candidates:
            try:
                resolved = candidate.resolve()
            except Exception:
                continue

            if resolved.exists() and resolved.is_file():
                return resolved

        return None

    def _read_pdf(
        self,
        path: Path,
    ) -> str:
        try:
            reader = PdfReader(str(path))
            pages_text = []

            for page in reader.pages:
                page_text = page.extract_text() or ""
                pages_text.append(page_text)

            return self._clean_text("\n".join(pages_text))
        except Exception as exc:
            raise HTTPException(
                status_code=400,
                detail=f"Could not read PDF text: {exc}",
            )

    def _read_basic_binary_document(
        self,
        path: Path,
    ) -> str:
        try:
            raw = path.read_bytes()
            decoded = raw.decode("utf-8", errors="ignore")
            cleaned = self._clean_text(decoded)

            if len(cleaned) >= 300:
                return cleaned

            return path.stem.replace("_", " ").replace("-", " ")
        except Exception:
            return path.stem.replace("_", " ").replace("-", " ")

    def _extract_concept_cards(
        self,
        text: str,
    ) -> list[ConceptCard]:
        sentences = self._split_sentences(text)
        cards: list[ConceptCard] = []
        seen_topics: set[str] = set()
        seen_facts: set[str] = set()

        for sentence in sentences:
            sentence = self._clean_text(sentence)

            if not self._is_good_fact_sentence(sentence):
                continue

            topic = self._extract_topic(sentence)

            if not topic:
                continue

            topic_key = topic.lower()
            fact_key = sentence.lower()

            if topic_key in seen_topics and fact_key in seen_facts:
                continue

            card = ConceptCard(
                topic=topic,
                fact=self._short_answer(sentence),
                source_snippet=self._short_snippet(sentence),
                category=self._category_for_topic(topic, sentence),
            )

            cards.append(card)
            seen_topics.add(topic_key)
            seen_facts.add(fact_key)

        if len(cards) < 6:
            cards.extend(
                self._fallback_cards_from_sentences(
                    sentences=sentences,
                    existing_facts=seen_facts,
                )
            )

        return cards

    def _fallback_cards_from_sentences(
        self,
        sentences: list[str],
        existing_facts: set[str],
    ) -> list[ConceptCard]:
        fallback_cards: list[ConceptCard] = []

        for sentence in sentences:
            sentence = self._clean_text(sentence)

            if sentence.lower() in existing_facts:
                continue

            if not self._is_good_fact_sentence(sentence):
                continue

            topic = self._first_important_phrase(sentence)

            if not topic:
                continue

            fallback_cards.append(
                ConceptCard(
                    topic=topic,
                    fact=self._short_answer(sentence),
                    source_snippet=self._short_snippet(sentence),
                    category=self._category_for_topic(topic, sentence),
                )
            )

            if len(fallback_cards) >= 8:
                break

        return fallback_cards

    def _build_question(
        self,
        card: ConceptCard,
        level: DifficultyLevel,
        question_number: int,
        all_cards: list[ConceptCard],
    ) -> dict[str, Any] | None:
        correct_answer = card.fact

        distractors = self._make_distractors(
            card=card,
            all_cards=all_cards,
        )

        if len(distractors) < 3:
            return None

        options = [
            correct_answer,
            distractors[0],
            distractors[1],
            distractors[2],
        ]

        options = self._unique_options(options)

        if len(options) < 4:
            return None

        random.shuffle(options)

        question_text = self._make_question_text(
            topic=card.topic,
            level=level,
            question_number=question_number,
            category=card.category,
        )

        explanation = self._make_explanation(
            card=card,
            level=level,
        )

        return {
            "question_text": question_text,
            "options": options,
            "correct_answer": correct_answer,
            "explanation": explanation,
            "source_chunk_snippet": card.source_snippet,
        }

    def _make_question_text(
        self,
        topic: str,
        level: DifficultyLevel,
        question_number: int,
        category: str,
    ) -> str:
        clean_topic = topic.strip()

        if level == DifficultyLevel.EASY:
            templates = [
                f"[EASY Q{question_number}] Which statement best defines {clean_topic}?",
                f"[EASY Q{question_number}] What does the material say about {clean_topic}?",
                f"[EASY Q{question_number}] Which option correctly describes {clean_topic}?",
            ]
            return random.choice(templates)

        if level == DifficultyLevel.MEDIUM:
            templates = [
                f"[MEDIUM Q{question_number}] Which option best explains the role of {clean_topic}?",
                f"[MEDIUM Q{question_number}] Based on the material, why does {clean_topic} matter?",
                f"[MEDIUM Q{question_number}] Which statement shows the best understanding of {clean_topic}?",
            ]
            return random.choice(templates)

        if category in {"process", "application", "quality"}:
            return (
                f"[HARD Q{question_number}] In a real system, which choice best applies the idea of {clean_topic}?"
            )

        return (
            f"[HARD Q{question_number}] Which answer shows the strongest understanding of {clean_topic}?"
        )

    def _make_explanation(
        self,
        card: ConceptCard,
        level: DifficultyLevel,
    ) -> str:
        if level == DifficultyLevel.EASY:
            prefix = "The answer is directly stated in the material."
        elif level == DifficultyLevel.MEDIUM:
            prefix = "The answer connects the topic to its meaning or role in the material."
        else:
            prefix = "The answer applies the material's idea to a more practical understanding."

        return f"{prefix} Source idea: {card.source_snippet}"

    def _make_distractors(
        self,
        card: ConceptCard,
        all_cards: list[ConceptCard],
    ) -> list[str]:
        distractors: list[str] = []

        other_cards = [
            other
            for other in all_cards
            if other.topic.lower() != card.topic.lower()
            and other.fact.lower() != card.fact.lower()
        ]

        random.shuffle(other_cards)

        for other in other_cards:
            distractors.append(other.fact)

            if len(distractors) >= 2:
                break

        distractors.extend(
            self._misconceptions_for_card(card)
        )

        final_distractors = []

        for item in distractors:
            clean_item = self._short_answer(item)

            if clean_item.lower() == card.fact.lower():
                continue

            if clean_item not in final_distractors:
                final_distractors.append(clean_item)

        return final_distractors[:3]

    def _misconceptions_for_card(
        self,
        card: ConceptCard,
    ) -> list[str]:
        topic = card.topic

        category_map = {
            "ai": [
                f"{topic} is only a fixed list of rules and cannot use data to improve decisions.",
                f"{topic} is mainly used for visual design and does not support reasoning or prediction.",
            ],
            "data": [
                f"{topic} has no effect on model behavior once the system is deployed.",
                f"{topic} is only used after testing and is not needed during model development.",
            ],
            "model": [
                f"{topic} is the same as a database table and does not learn patterns.",
                f"{topic} only stores files and does not process information.",
            ],
            "evaluation": [
                f"{topic} is used to choose screen colors, not to measure model performance.",
                f"{topic} is only checked before training and never after the model is built.",
            ],
            "process": [
                f"{topic} is a single action that does not involve feedback or improvement over time.",
                f"{topic} is unrelated to planning, testing, or real application behavior.",
            ],
            "quality": [
                f"{topic} only affects the appearance of the app, not how reliable the system is.",
                f"{topic} is not important after the first version of the system is released.",
            ],
            "application": [
                f"{topic} is only a theory concept and cannot be connected to real software systems.",
                f"{topic} only works without APIs, backend services, or user-facing applications.",
            ],
            "general": [
                f"{topic} is not connected to the main topic explained in the material.",
                f"{topic} is only a label and does not describe a real concept or process.",
            ],
        }

        choices = category_map.get(card.category, category_map["general"])
        random.shuffle(choices)
        return choices

    def _category_for_topic(
        self,
        topic: str,
        sentence: str,
    ) -> str:
        combined = f"{topic} {sentence}".lower()

        if any(
            word in combined
            for word in [
                "artificial intelligence",
                " ai ",
                "machine learning",
                "deep learning",
                "nlp",
                "computer vision",
                "reinforcement learning",
            ]
        ):
            return "ai"

        if any(
            word in combined
            for word in [
                "data",
                "dataset",
                "training",
                "examples",
                "representative",
            ]
        ):
            return "data"

        if any(
            word in combined
            for word in [
                "model",
                "neural network",
                "network",
                "layers",
                "prediction",
            ]
        ):
            return "model"

        if any(
            word in combined
            for word in [
                "accuracy",
                "precision",
                "recall",
                "f1",
                "loss",
                "metric",
                "evaluat",
            ]
        ):
            return "evaluation"

        if any(
            word in combined
            for word in [
                "process",
                "phase",
                "cycle",
                "workflow",
                "pipeline",
                "agent",
                "environment",
                "reward",
            ]
        ):
            return "process"

        if any(
            word in combined
            for word in [
                "reliable",
                "reliability",
                "scalable",
                "scalability",
                "security",
                "privacy",
                "fairness",
                "safety",
                "ethical",
            ]
        ):
            return "quality"

        if any(
            word in combined
            for word in [
                "application",
                "api",
                "frontend",
                "backend",
                "server",
                "interface",
                "software system",
            ]
        ):
            return "application"

        return "general"

    def _remove_used_cards(
        self,
        cards: list[ConceptCard],
        old_source_snippets: list[str],
    ) -> list[ConceptCard]:
        if not old_source_snippets:
            return cards

        fresh_cards: list[ConceptCard] = []

        for card in cards:
            card_snippet = self._clean_text(card.source_snippet).lower()
            was_used = False

            for old_snippet in old_source_snippets:
                old = self._clean_text(old_snippet or "").lower()

                if old and (old in card_snippet or card_snippet in old):
                    was_used = True
                    break

            if not was_used:
                fresh_cards.append(card)

        return fresh_cards

    def _extract_topic(
        self,
        sentence: str,
    ) -> str | None:
        known_topic = self._known_topic(sentence)

        if known_topic:
            return known_topic

        patterns = [
            r"^([A-Za-z][A-Za-z0-9\-]*(?:\s+[A-Za-z][A-Za-z0-9\-]*){0,4}),?\s+(?:also called\s+[A-Za-z0-9\- ]+,?\s+)?(?:is|are|means|refers to|describes|focuses on)\b",
            r"^([A-Za-z][A-Za-z0-9\-]*(?:\s+[A-Za-z][A-Za-z0-9\-]*){0,4})\s+(?:happens|allows|helps|contains|includes|uses|stores|supports)\b",
            r"(?:called|known as)\s+([A-Za-z][A-Za-z0-9\-]*(?:\s+[A-Za-z][A-Za-z0-9\-]*){0,4})",
            r"(?:such as|include|includes)\s+([A-Za-z][A-Za-z0-9\-]*(?:\s+[A-Za-z][A-Za-z0-9\-]*){0,3})",
        ]

        for pattern in patterns:
            match = re.search(pattern, sentence, flags=re.IGNORECASE)

            if match:
                candidate = self._clean_topic(match.group(1))

                if self._is_valid_topic(candidate):
                    return candidate

        return self._first_important_phrase(sentence)

    def _known_topic(
        self,
        sentence: str,
    ) -> str | None:
        lower = sentence.lower()

        known_topics = [
            "artificial intelligence",
            "machine learning",
            "training data",
            "neural network",
            "neural networks",
            "deep learning",
            "natural language processing",
            "computer vision",
            "reinforcement learning",
            "evaluation metrics",
            "accuracy",
            "precision",
            "recall",
            "f1 score",
            "loss",
            "overfitting",
            "validation data",
            "regularization",
            "responsible ai",
            "privacy",
            "fairness",
            "security",
            "explainability",
            "software systems",
            "apis",
            "api",
            "frontend app",
            "backend server",
            "software engineering",
            "functional requirements",
            "non-functional requirements",
            "sdlc",
            "frontend development",
            "backend development",
            "database",
            "authentication",
            "authorization",
            "testing",
            "integration testing",
            "maintainable code",
            "scalability",
            "reliability",
        ]

        for topic in known_topics:
            if topic in lower:
                return self._display_topic(topic)

        return None

    def _display_topic(
        self,
        topic: str,
    ) -> str:
        special = {
            "ai": "AI",
            "apis": "APIs",
            "api": "API",
            "nlp": "NLP",
            "sdlc": "SDLC",
            "f1 score": "F1 score",
            "responsible ai": "responsible AI",
        }

        if topic in special:
            return special[topic]

        return topic.title()

    def _first_important_phrase(
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
            "system",
            "systems",
            "usually",
            "include",
            "includes",
            "common",
            "example",
        }

        candidates = [
            word
            for word in words
            if word.lower() not in bad_words
        ]

        if not candidates:
            return None

        candidates.sort(key=len, reverse=True)
        return self._clean_topic(candidates[0])

    def _clean_topic(
        self,
        topic: str,
    ) -> str:
        topic = self._clean_text(topic)
        topic = re.sub(r"^(a|an|the)\s+", "", topic, flags=re.IGNORECASE)
        topic = topic.strip(" .,:;-")
        return topic

    def _is_valid_topic(
        self,
        topic: str | None,
    ) -> bool:
        if not topic:
            return False

        lower = topic.lower()

        blocked = {
            "the",
            "this",
            "that",
            "these",
            "those",
            "good",
            "poor",
            "common",
            "example",
            "tasks",
            "systems",
        }

        if lower in blocked:
            return False

        return 3 <= len(topic) <= 60

    def _is_good_fact_sentence(
        self,
        sentence: str,
    ) -> bool:
        if len(sentence) < 45 or len(sentence) > 260:
            return False

        lower = sentence.lower()

        bad_fragments = [
            "review questions",
            "what is ",
            "why is ",
            "how does ",
            "which ",
            "download",
            "click",
        ]

        if any(fragment in lower for fragment in bad_fragments):
            return False

        good_words = [
            " is ",
            " are ",
            " means ",
            " refers ",
            " focuses ",
            " allows ",
            " helps ",
            " contains ",
            " includes ",
            " used ",
            " important ",
            " can ",
            " should ",
            " happens ",
            " receives ",
            " learns ",
            " evaluates ",
        ]

        return any(word in lower for word in good_words)

    def _split_sentences(
        self,
        text: str,
    ) -> list[str]:
        text = self._clean_text(text)
        parts = re.split(r"(?<=[.!?])\s+", text)

        sentences = []

        for part in parts:
            clean_part = self._clean_text(part)

            if clean_part:
                sentences.append(clean_part)

        return sentences

    def _remove_review_section(
        self,
        text: str,
    ) -> str:
        match = re.search(r"\breview questions\b", text, flags=re.IGNORECASE)

        if match:
            return text[: match.start()].strip()

        return text

    def _unique_options(
        self,
        options: list[str],
    ) -> list[str]:
        final_options: list[str] = []
        seen: set[str] = set()

        for option in options:
            clean_option = self._clean_text(option)

            if not clean_option:
                continue

            key = clean_option.lower()

            if key in seen:
                continue

            seen.add(key)
            final_options.append(clean_option)

        return final_options

    def _was_used_before(
        self,
        question_text: str,
        old_question_texts: list[str],
    ) -> bool:
        new_question = self._clean_text(question_text).lower()

        for old_question in old_question_texts:
            if self._clean_text(old_question).lower() == new_question:
                return True

        return False

    def _is_duplicate_question(
        self,
        question: dict[str, Any],
        questions: list[dict[str, Any]],
    ) -> bool:
        new_text = self._clean_text(question["question_text"]).lower()
        new_answer = self._clean_text(question["correct_answer"]).lower()

        for old in questions:
            old_text = self._clean_text(old["question_text"]).lower()
            old_answer = self._clean_text(old["correct_answer"]).lower()

            if new_text == old_text:
                return True

            if new_answer == old_answer:
                return True

        return False

    def _question_count(
        self,
        difficulty: DifficultyLevel,
    ) -> int:
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
        if difficulty == DifficultyLevel.EASY:
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

        while len(plan) < count:
            plan.append(DifficultyLevel.MEDIUM)

        return plan[:count]

    def _short_answer(
        self,
        text: str,
    ) -> str:
        text = self._clean_text(text)

        if len(text) <= 180:
            return text

        return text[:177].rstrip() + "..."

    def _short_snippet(
        self,
        text: str,
    ) -> str:
        text = self._clean_text(text)

        if len(text) <= 300:
            return text

        return text[:297].rstrip() + "..."

    def _clean_text(
        self,
        text: str,
    ) -> str:
        return re.sub(r"\s+", " ", text or "").strip()


ai_service = AIPipelineService()