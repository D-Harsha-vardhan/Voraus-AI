import requests
import json
import time

api_key = "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"
url = "https://integrate.api.nvidia.com/v1/chat/completions"

headers = {
    "Authorization": f"Bearer {api_key}",
    "Accept": "application/json",
    "Content-Type": "application/json"
}

models_to_test = [
    "mistralai/mixtral-8x7b-instruct-v0.1",
    "meta/llama2-70b",
    "meta/codellama-70b",
    "meta/llama-3.2-11b-vision-instruct",
    "meta/llama-3.2-90b-vision-instruct",
]

for model in models_to_test:
    payload = {
        "model": model,
        "messages": [{"role": "user", "content": "Hello"}],
        "max_tokens": 10
    }
    response = requests.post(url, headers=headers, json=payload)
    print(f"Model: {model}, Status: {response.status_code}")
    if response.status_code == 200:
        print("SUCCESS")
        break
