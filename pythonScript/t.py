import sys
import io
import json

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

test_data = [
    {'text': '안녕하세요', 'confidence': 0.95},
    {'text': 'Hello', 'confidence': 0.98},
    {'text': '테스트', 'confidence': 0.92}
]

print(json.dumps(test_data, ensure_ascii=False))
sys.stdout.flush()