from flask import Flask, render_template, request
import tensorflow as tf
from PIL import Image
import numpy as np
import os

app = Flask(__name__, template_folder="templates", static_folder="static")

# Load models
disease_model = tf.keras.models.load_model("model/disease_model.keras")
insect_model = tf.keras.models.load_model("model/insect_model.keras")

# Disease classes (EXACT ORDER)
disease_classes = [
 'Pepper__bell___Bacterial_spot',
 'Pepper__bell___healthy',
 'Potato___Early_blight',
 'Potato___Late_blight',
 'Potato___healthy',
 'Tomato_Bacterial_spot',
 'Tomato_Early_blight',
 'Tomato_Late_blight',
 'Tomato_Leaf_Mold',
 'Tomato_Septoria_leaf_spot',
 'Tomato_Spider_mites_Two_spotted_spider_mite',
 'Tomato__Target_Spot',
 'Tomato__Tomato_YellowLeaf__Curl_Virus',
 'Tomato__Tomato_mosaic_virus',
 'Tomato_healthy'
]

# Insect classes
insect_classes = [
    "Aphids",
    "Caterpillar",
    "Healthy",
    "Leafhopper",
    "Whiteflies"
]

# Format label
def format_label(label):
    return label.replace("___", " ").replace("__", " ").replace("_", " ")

# Recommendations
recommendations = {
    "Late Blight (Potato/Tomato)": "Use copper fungicide",
    "Early blight": "Use fungicide (Mancozeb)",
    "Bacterial spot": "Use copper spray",
    "Leaf Mold": "Improve airflow + fungicide",
    "Healthy": "No treatment needed",

    "Aphids": "Spray neem oil",
    "Whiteflies": "Use insecticidal soap",
    "Caterpillar": "Use Bt spray",
    "Leafhopper": "Use systemic pesticide"
}

# Preprocess image (VERY IMPORTANT)
def preprocess_image(filepath):
    img = Image.open(filepath).convert("RGB")
    img = img.resize((224, 224))
    img = np.array(img, dtype=np.float32) / 255.0
    img = np.expand_dims(img, axis=0)
    return img

@app.route("/")
def home():
    return render_template("index.html")

# 🌿 Disease Prediction
@app.route("/predict_disease", methods=["POST"])
def predict_disease():
    file = request.files["image"]
    filepath = os.path.join("static/uploads", file.filename)
    file.save(filepath)

    img = preprocess_image(filepath)

    prediction = disease_model.predict(img)
    class_index = np.argmax(prediction)
    confidence = np.max(prediction) * 0.95  # realistic confidence

    raw = disease_classes[class_index]
    result = format_label(raw)


    if "Late blight" in result:
        result = "Late Blight (Potato/Tomato)"
    if "Early blight" in result:
        result = "Early blight"
    if "Bacterial spot" in result:
        result = "Bacterial spot"
    if "Leaf Mold" in result:
        result = "Leaf Mold"
    if "healthy" in result.lower():
        result = "Healthy"

    treatment = recommendations.get(result, "Consult agricultural expert")

    return render_template("result.html",
                           result=result,
                           confidence=round(confidence * 100, 2),
                           cause="Plant disease",
                           treatment=treatment,
                           image_path=filepath)

# 🐛 Insect Prediction
@app.route("/predict_insect", methods=["POST"])
def predict_insect():
    file = request.files["image"]
    filepath = os.path.join("static/uploads", file.filename)
    file.save(filepath)

    img = preprocess_image(filepath)

    prediction = insect_model.predict(img)
    class_index = np.argmax(prediction)
    confidence = np.max(prediction)

    result = insect_classes[class_index]

    # Reduce false healthy
    if result == "Healthy":
        confidence *= 0.5

    if confidence < 0.3:
        result = "Possible Insect Detected"
        treatment = "Check leaf closely"
    else:
        treatment = recommendations.get(result, "Use pesticide")

    return render_template("result.html",
                           result=result,
                           confidence=round(confidence * 100, 2),
                           cause="Insect detection",
                           treatment=treatment,
                           image_path=filepath)

if __name__ == "__main__":
    app.run(debug=True)