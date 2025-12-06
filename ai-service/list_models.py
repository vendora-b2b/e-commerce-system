import google.generativeai as genai

genai.configure(api_key='AIzaSyBWVcLw0VJVv5GuW92AtrDx2lIrFlU8l1Q')

print("\n=== Available Gemini Models that support generateContent ===\n")
for model in genai.list_models():
    if 'generateContent' in model.supported_generation_methods:
        print(f"Name: {model.name}")
        print(f"Display Name: {model.display_name}")
        print(f"Description: {model.description}")
        print("-" * 60)
