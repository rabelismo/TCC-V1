"""Mede, por módulo, quantas execuções Python uma submissão dispara e se o
expectedOutput do módulo (exposto em GET /api/modules) é a resposta de um critério.

Uso: python docs/refactor/measure-runs.py
"""
import json
import pathlib

MODULES = pathlib.Path(__file__).resolve().parents[2] / "backend/src/main/resources/modules"

for path in sorted(MODULES.glob("module-*.json")):
    data = json.loads(path.read_text(encoding="utf-8"))
    criteria = data.get("criteria") or []
    own_input = [c for c in criteria if c["type"] == "TEST_CASE" and c.get("input")]
    shared = len(criteria) > len(own_input)
    runs = len(own_input) + (1 if shared else 0)
    expected = (data.get("expectedOutput") or "").strip()
    leaks = bool(expected) and any(c["expectedOutput"].strip() == expected for c in criteria)
    print(f"{path.name}: criterios={len(criteria)} execucoes={runs} expectedOutput_igual_criterio={leaks}")
