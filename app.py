from flask import Flask, render_template, request
import tensorflow as tf
from PIL import Image
import numpy as np
import os

app = Flask(__name__, template_folder="templates", static_folder="static")

# Load models
disease_model = tf.keras.models.load_model("model/disease_model.keras")
insect_model = tf.keras.models.load_model("model/insect_model.keras")

# Disease classes (EXACT ORDER MATCHING MODEL TRAINING)
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

# Format label for clean display
def format_label(label):
    label_map = {
        'Pepper__bell___Bacterial_spot': 'Pepper Bell - Bacterial Spot',
        'Pepper__bell___healthy': 'Pepper Bell - Healthy',
        'Potato___Early_blight': 'Potato - Early Blight',
        'Potato___Late_blight': 'Potato - Late Blight',
        'Potato___healthy': 'Potato - Healthy',
        'Tomato_Bacterial_spot': 'Tomato - Bacterial Spot',
        'Tomato_Early_blight': 'Tomato - Early Blight',
        'Tomato_Late_blight': 'Tomato - Late Blight',
        'Tomato_Leaf_Mold': 'Tomato - Leaf Mold',
        'Tomato_Septoria_leaf_spot': 'Tomato - Septoria Leaf Spot',
        'Tomato_Spider_mites_Two_spotted_spider_mite': 'Tomato - Spider Mites (Two-Spotted)',
        'Tomato__Target_Spot': 'Tomato - Target Spot',
        'Tomato__Tomato_YellowLeaf__Curl_Virus': 'Tomato - Yellow Leaf Curl Virus',
        'Tomato__Tomato_mosaic_virus': 'Tomato - Mosaic Virus',
        'Tomato_healthy': 'Tomato - Healthy'
    }
    if label in label_map:
        return label_map[label]
    
    cleaned = label.replace("___", " - ").replace("__", " ").replace("_", " ")
    return " ".join(word.capitalize() for word in cleaned.split())

# Complete recommendations for all disease & insect classes
recommendations = {
    "Pepper Bell - Bacterial Spot": "Spray copper-based bactericides; use pathogen-free seeds.",
    "Pepper Bell - Healthy": "Plant is healthy! Maintain regular watering and soil nutrition.",
    "Potato - Early Blight": "Apply protective fungicides (Mancozeb or Chlorothalonil); rotate crops.",
    "Potato - Late Blight": "Apply copper fungicides or systemic treatments; destroy infected plant debris.",
    "Potato - Healthy": "Plant is healthy! Maintain proper soil drainage and moisture.",
    "Tomato - Bacterial Spot": "Use copper-based sprays; avoid overhead irrigation.",
    "Tomato - Early Blight": "Apply copper or Mancozeb fungicide; prune lower leaves near soil.",
    "Tomato - Late Blight": "Apply systemic fungicides immediately; remove heavily infected foliage.",
    "Tomato - Leaf Mold": "Improve greenhouse airflow/ventilation and lower humidity.",
    "Tomato - Septoria Leaf Spot": "Apply fungicides; remove fallen leaf debris and practice crop rotation.",
    "Tomato - Spider Mites (Two-Spotted)": "Apply insecticidal soap or neem oil; maintain adequate humidity.",
    "Tomato - Target Spot": "Apply protective fungicides; improve plant spacing for airflow.",
    "Tomato - Yellow Leaf Curl Virus": "Control whiteflies using yellow sticky traps/neem oil; remove infected plants.",
    "Tomato - Mosaic Virus": "Disinfect gardening tools and destroy infected plants (no chemical cure).",
    "Tomato - Healthy": "Plant is healthy! Continue good cultural practices.",

    "Aphids": "Spray neem oil or insecticidal soap on undersides of leaves.",
    "Caterpillar": "Apply Bacillus thuringiensis (Bt) spray or handpick caterpillars.",
    "Healthy": "No insect pest infestation detected.",
    "Leafhopper": "Use insecticidal soap or yellow sticky cards; remove weeds.",
    "Whiteflies": "Apply neem oil or insecticidal soap; place yellow sticky traps."
}

# Preprocess image
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
    if "image" not in request.files:
        return render_template("index.html", error="No image uploaded")
        
    file = request.files["image"]
    if file.filename == "":
        return render_template("index.html", error="No selected file")

    filepath = os.path.join("static/uploads", file.filename)
    file.save(filepath)

    img = preprocess_image(filepath)

    prediction = disease_model.predict(img)
    class_index = int(np.argmax(prediction[0]))
    confidence = float(np.max(prediction[0]))

    raw = disease_classes[class_index]
    result = format_label(raw)

    treatment = recommendations.get(result, "Consult an agricultural expert for specific diagnosis.")

    return render_template("result.html",
                           result=result,
                           confidence=round(confidence * 100, 2),
                           cause="Plant disease",
                           treatment=treatment,
                           image_path=filepath)

# 🐛 Insect Prediction
@app.route("/predict_insect", methods=["POST"])
def predict_insect():
    if "image" not in request.files:
        return render_template("index.html", error="No image uploaded")

    file = request.files["image"]
    if file.filename == "":
        return render_template("index.html", error="No selected file")

    filepath = os.path.join("static/uploads", file.filename)
    file.save(filepath)

    img = preprocess_image(filepath)

    prediction = insect_model.predict(img)
    class_index = int(np.argmax(prediction[0]))
    confidence = float(np.max(prediction[0]))

    result = insect_classes[class_index]

    if confidence < 0.35:
        display_result = "Uncertain Insect Detection"
        treatment = "Low confidence detection. Please inspect leaf closely or upload a clearer photo."
    else:
        display_result = result if result != "Healthy" else "Healthy (No Pest Found)"
        treatment = recommendations.get(result, "Consult an agricultural expert.")

    return render_template("result.html",
                           result=display_result,
                           confidence=round(confidence * 100, 2),
                           cause="Insect detection",
                           treatment=treatment,
                           image_path=filepath)

if __name__ == "__main__":
    app.run(debug=True)
