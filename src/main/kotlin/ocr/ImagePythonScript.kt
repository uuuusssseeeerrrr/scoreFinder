package com.score.ocr

object ImagePythonScript {
    val pythonScript = """
        import sys
        import io
        import json
        import os
        import easyocr
        
        # 가장 먼저 UTF-8 설정
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
        sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
        
        def recognize_text(image_path):
            try:
                reader = easyocr.Reader(['ko', 'en'], gpu=True, verbose=False)
                results = reader.readtext(image_path)
        
                output = []
                for (bbox, text, prob) in results:
                    output.append({
                        'text': text,
                        'confidence': float(prob),
                        'bbox': [[float(x), float(y)] for x, y in bbox]
                    })
        
                # JSON 출력
                result_json = json.dumps(output, ensure_ascii=False)
                print(result_json)
                sys.stdout.flush()
        
            except Exception as e:
                error_json = json.dumps({'error': str(e)}, ensure_ascii=False)
                print(error_json)
                sys.stderr.flush()
                sys.exit(1)
        
        if __name__ == "__main__":
            if len(sys.argv) < 2:
                print(json.dumps({"error": "이미지 경로 필요"}, ensure_ascii=False))
                sys.exit(1)
        
            image_path = sys.argv[1]
            recognize_text(image_path)
    """.trimIndent()
}