import base64
import requests
from flask import Flask, request, jsonify
from dotenv import load_dotenv
import sys
import os
from pathlib import Path

if getattr(sys, 'frozen', False):
    BASE_DIR = Path(sys.executable).parent
else:
    BASE_DIR = Path(__file__).parent

load_dotenv(BASE_DIR / '.env')

app = Flask(__name__)

API_KEY = os.getenv("DOUBAO_API_KEY")
TEXT_EP = os.getenv("TEXT_ENDPOINT")        # 文本和视觉共用此端点
IMAGE_EP = os.getenv("IMAGE_ENDPOINT")

HEADERS = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

MODEL_NAME = "doubao-seed-2-0-pro-260215"   # 既支持文本也支持视觉

# ========== 辅助函数 ==========
def extract_text(result):
    """从豆包 responses API 返回结果中提取文本"""
    try:
        for item in result["output"]:
            if item.get("type") == "message":
                for content_item in item.get("content", []):
                    if content_item.get("type") == "output_text":
                        return content_item["text"]
        return str(result)
    except (KeyError, IndexError, TypeError):
        return str(result)


def build_vision_payload(image_data_uri, user_text=None):
    """构建视觉请求的 payload，包含图片和可选文本"""
    content = [
        {"type": "input_image", "image_url": image_data_uri}
    ]
    # 如果用户提供了文本，使用它；否则使用默认提示
    text = user_text if user_text else "请详细描述这张图片的内容，并提取其中出现的所有文字。"
    content.append({"type": "input_text", "text": text})

    return {
        "model": MODEL_NAME,
        "input": [
            {
                "role": "user",
                "content": content
            }
        ]
    }


# ========== 统一对话接口（支持纯文本和图片） ==========
@app.route("/chat", methods=["POST"])
def chat():
    # 1. 判断请求类型
    if request.content_type and "multipart/form-data" in request.content_type:
        # -------- multipart 请求（可能包含图片）--------
        user_message = request.form.get("message", "")  # 可选文本
        image_file = request.files.get("image")         # 可选图片

        if not image_file and not user_message:
            return jsonify({"error": "message or image is required"}), 400

        # 如果没有图片，作为纯文本处理
        if not image_file:
            payload = {
                "model": MODEL_NAME,
                "input": [
                    {
                        "role": "user",
                        "content": [
                            {"type": "input_text", "text": user_message}
                        ]
                    }
                ]
            }
        else:
            # 有图片，读取并转为 base64
            img_bytes = image_file.read()
            mime_type = image_file.mimetype or "image/jpeg"
            img_base64 = base64.b64encode(img_bytes).decode("utf-8")
            image_data_uri = f"data:{mime_type};base64,{img_base64}"
            payload = build_vision_payload(image_data_uri, user_message)

    else:
        # -------- JSON 请求（纯文本）--------
        data = request.get_json()
        if not data:
            return jsonify({"error": "Invalid JSON"}), 400
        user_message = data.get("message", "")
        if not user_message:
            return jsonify({"error": "message is required"}), 400

        payload = {
            "model": MODEL_NAME,
            "input": [
                {
                    "role": "user",
                    "content": [
                        {"type": "input_text", "text": user_message}
                    ]
                }
            ]
        }

    # 2. 调用豆包 API（文本和视觉都用 TEXT_EP）
    resp = requests.post(TEXT_EP, headers=HEADERS, json=payload)
    if resp.status_code != 200:
        return jsonify({"error": resp.text}), resp.status_code

    result = resp.json()
    answer = extract_text(result)

    # 3. 打印日志（便于调试）
    print("=" * 30)
    print("User input:", user_message if 'user_message' in locals() else "[multipart]")
    print("Reply:", answer)
    print("=" * 30)

    return jsonify({"reply": answer})


# ========== 图生图（保持不变） ==========
@app.route("/generate-image", methods=["POST"])
def generate_image():
    if "image" not in request.files:
        return jsonify({"error": "image file is required"}), 400
    prompt = request.form.get("prompt", "")

    image_file = request.files["image"]
    img_bytes = image_file.read()
    mime_type = image_file.mimetype or "image/jpeg"
    img_base64 = base64.b64encode(img_bytes).decode("utf-8")
    image_data_uri = f"data:{mime_type};base64,{img_base64}"

    payload = {
        "model": "doubao-seedream-5-0-260128",
        "prompt": prompt,
        "image": image_data_uri,
        "strength": 0.7,
        "size": "2K",
        "response_format": "url",
        "stream": False,
        "watermark": True
    }

    resp = requests.post(IMAGE_EP, headers=HEADERS, json=payload)
    if resp.status_code != 200:
        return jsonify({"error": resp.text}), resp.status_code

    result = resp.json()
    try:
        image_url = result["data"][0]["url"]
    except KeyError:
        image_url = str(result)

    print("=" * 30)
    print("Generate image prompt:", prompt)
    print("Image URL:", image_url)
    print("=" * 30)

    return jsonify({"image_url": image_url})


# 健康检查
@app.route("/")
def index():
    return "Doubao Proxy is running"


if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    app.run(host="0.0.0.0", port=port, debug=False)