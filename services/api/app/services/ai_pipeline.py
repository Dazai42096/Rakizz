from pathlib import Path
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
    ) -> list[dict[str, Any]]:
        # read the uploaded material first
        text = self._read_material_text(material_url)

        if not text.strip():
            raise HTTPException(
                status_code=400,
                detail="Could not read text from this material",
            )

        # make real questions from the material text
        questions = self._build_questions_from_text(
            text=text,
            difficulty=difficulty,
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
            # if it is not a local file, use the url text as fallback
            return material_url

        suffix = path.suffix.lower()

        if suffix == ".pdf":
            return self._read_pdf(path)

        if suffix == ".txt":
            return path.read_text(
                encoding="utf-8",
                errors="ignore",
            )

        # simple fallback for other files
        return path.name.replace("_", " ").replace("-", " ")

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

    def _build_questions_from_text(
        self,
        text: str,
        difficulty: DifficultyLevel,
    ) -> list[dict[str, Any]]:
        clean_text = self._clean_text(text)
        lower_text = clean_text.lower()

        questions: list[dict[str, Any]] = []

        # these are simple content based questions for the checkpoint
        if "lan" in lower_text and "wan" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="What is the main difference between a LAN and a WAN?",
                    options=[
                        "A LAN covers a small local area, while a WAN covers a larger geographic area.",
                        "A LAN is always wireless, while a WAN is always wired.",
                        "A LAN only works without internet, while a WAN only works with Bluetooth.",
                        "A LAN is used only for phones, while a WAN is used only for printers.",
                    ],
                    correct="A LAN covers a small local area, while a WAN covers a larger geographic area.",
                    explanation="A LAN is used in a limited place like a home or school, while a WAN connects larger areas.",
                    snippet=self._find_snippet(clean_text, "LAN"),
                )
            )

        if "client" in lower_text and "server" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="In a client-server network, what does the client usually do?",
                    options=[
                        "It requests services or data from a server.",
                        "It replaces the router inside the network.",
                        "It stores the MAC address of every device only.",
                        "It blocks all traffic from the internet.",
                    ],
                    correct="It requests services or data from a server.",
                    explanation="The client sends requests, and the server responds with data or services.",
                    snippet=self._find_snippet(clean_text, "client"),
                )
            )

        if "router" in lower_text and "switch" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="What is the main job of a router?",
                    options=[
                        "To connect different networks and forward traffic between them.",
                        "To connect only devices inside the same local network.",
                        "To convert HTTP websites into HTTPS websites.",
                        "To store quiz answers for students.",
                    ],
                    correct="To connect different networks and forward traffic between them.",
                    explanation="A router connects networks, while a switch mainly connects devices in one local network.",
                    snippet=self._find_snippet(clean_text, "router"),
                )
            )

        if "ip address" in lower_text and "mac address" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="Which statement correctly describes an IP address and a MAC address?",
                    options=[
                        "An IP address is used for network location, while a MAC address identifies a network device.",
                        "An IP address is permanent hardware, while a MAC address changes every second.",
                        "An IP address is used only by printers, while a MAC address is used only by websites.",
                        "Both IP and MAC addresses are the same thing.",
                    ],
                    correct="An IP address is used for network location, while a MAC address identifies a network device.",
                    explanation="The IP address helps route data, while the MAC address identifies the device hardware on a network.",
                    snippet=self._find_snippet(clean_text, "IP address"),
                )
            )

        if "osi" in lower_text or "application layer" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="Which OSI layer is closest to the user and supports services like web browsing?",
                    options=[
                        "Application layer",
                        "Physical layer",
                        "Data link layer",
                        "Transport layer",
                    ],
                    correct="Application layer",
                    explanation="The application layer is closest to the user and supports apps like web browsers and email.",
                    snippet=self._find_snippet(clean_text, "Application"),
                )
            )

        if "tcp" in lower_text and "udp" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="Why is TCP usually used when reliability is important?",
                    options=[
                        "Because it checks delivery and can resend lost data.",
                        "Because it never creates a connection.",
                        "Because it is only used for DNS.",
                        "Because it blocks all packets before sending them.",
                    ],
                    correct="Because it checks delivery and can resend lost data.",
                    explanation="TCP is reliable because it confirms delivery and can retransmit missing data.",
                    snippet=self._find_snippet(clean_text, "TCP"),
                )
            )

        if "dns" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="What does DNS do?",
                    options=[
                        "It translates domain names into IP addresses.",
                        "It encrypts every file on the computer.",
                        "It controls the brightness of the screen.",
                        "It removes all traffic from the router.",
                    ],
                    correct="It translates domain names into IP addresses.",
                    explanation="DNS helps users access websites by converting names like example.com into IP addresses.",
                    snippet=self._find_snippet(clean_text, "DNS"),
                )
            )

        if "https" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="Why is HTTPS more secure than HTTP?",
                    options=[
                        "HTTPS encrypts data between the browser and the website.",
                        "HTTPS removes the need for an IP address.",
                        "HTTPS is only used for local networks.",
                        "HTTPS makes a computer faster by increasing RAM.",
                    ],
                    correct="HTTPS encrypts data between the browser and the website.",
                    explanation="HTTPS protects data using encryption, which makes communication safer than plain HTTP.",
                    snippet=self._find_snippet(clean_text, "HTTPS"),
                )
            )

        if "bandwidth" in lower_text and "latency" in lower_text:
            questions.append(
                self._question(
                    difficulty=difficulty,
                    question="What does latency measure in a network?",
                    options=[
                        "The delay before data starts arriving.",
                        "The total number of saved files.",
                        "The size of a monitor screen.",
                        "The number of installed apps.",
                    ],
                    correct="The delay before data starts arriving.",
                    explanation="Latency is delay. Lower latency usually means a faster response.",
                    snippet=self._find_snippet(clean_text, "latency"),
                )
            )

        # if the material is different, still make questions from its sentences
        if len(questions) < 5:
            questions.extend(
                self._fallback_questions(
                    text=clean_text,
                    difficulty=difficulty,
                    already_count=len(questions),
                )
            )

        question_count = self._question_count(difficulty)

        return questions[:question_count]

    def _fallback_questions(
        self,
        text: str,
        difficulty: DifficultyLevel,
        already_count: int,
    ) -> list[dict[str, Any]]:
        sentences = self._split_sentences(text)
        results = []

        for sentence in sentences:
            if len(results) + already_count >= 5:
                break

            important_word = self._pick_keyword(sentence)

            if not important_word:
                continue

            results.append(
                self._question(
                    difficulty=difficulty,
                    question=f"According to the material, which idea is connected to {important_word}?",
                    options=[
                        sentence[:120],
                        "It is not mentioned in the uploaded material.",
                        "It is only related to entertainment apps.",
                        "It means the same thing as a password reset.",
                    ],
                    correct=sentence[:120],
                    explanation="The correct answer is taken from the uploaded material.",
                    snippet=sentence[:180],
                )
            )

        return results

    def _question(
        self,
        difficulty: DifficultyLevel,
        question: str,
        options: list[str],
        correct: str,
        explanation: str,
        snippet: str,
    ) -> dict[str, Any]:
        return {
            "question_text": f"{question} ({difficulty.value})",
            "options": options,
            "correct_answer": correct,
            "explanation": explanation,
            "source_chunk_snippet": snippet,
        }

    def _question_count(
        self,
        difficulty: DifficultyLevel,
    ) -> int:
        if difficulty == DifficultyLevel.EASY:
            return 5

        if difficulty == DifficultyLevel.MEDIUM:
            return 6

        return 7

    def _clean_text(
        self,
        text: str,
    ) -> str:
        return re.sub(r"\s+", " ", text).strip()

    def _split_sentences(
        self,
        text: str,
    ) -> list[str]:
        parts = re.split(r"(?<=[.!?])\s+", text)

        return [
            part.strip()
            for part in parts
            if len(part.strip()) >= 40
        ]

    def _pick_keyword(
        self,
        sentence: str,
    ) -> str | None:
        words = re.findall(r"[A-Za-z][A-Za-z\-]{3,}", sentence)

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
        }

        for word in words:
            if word.lower() not in bad_words:
                return word

        return None

    def _find_snippet(
        self,
        text: str,
        keyword: str,
    ) -> str:
        index = text.lower().find(keyword.lower())

        if index == -1:
            return text[:180]

        start = max(index - 80, 0)
        end = min(index + 180, len(text))

        return text[start:end].strip()


ai_service = AIPipelineService()