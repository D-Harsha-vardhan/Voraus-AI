import requests

invoke_url = "https://integrate.api.nvidia.com/v1/chat/completions"

headers = {
    "Authorization": "Bearer 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",
    "Accept": "application/json",
}

# Download the dummy passport/image
img_url = "https://assets.ngc.nvidia.com/products/api-catalog/phi-3-5-vision/example1a.jpg"
import base64
img_data = requests.get(img_url).content
b64 = base64.b64encode(img_data).decode("utf-8")
data_url = f"data:image/jpeg;base64,{b64}"

payload = {
  "messages": [
    {
      "content": [
        {
          "image_url": {
            "url": data_url
          },
          "type": "image_url"
        },
        {
          "type": "text",
          "text": "Transcribe all text from this image and structure it. Return ONLY a valid JSON object where keys are the logical field names (in snake_case, e.g. given_name, date_of_birth, technical_skills, etc) and values are the extracted text. Do NOT include any explanations, safety warnings, or markdown blocks."
        }
      ],
      "role": "user"
    }
  ],
  "model": "meta/llama-3.2-11b-vision-instruct",
  "max_tokens": 2048,
  "temperature": 0.0,
  "response_format": { "type": "json_object" }
}

response = requests.post(invoke_url, headers=headers, json=payload, timeout=20)
print(response.status_code)
print(response.text)
