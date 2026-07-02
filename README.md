# Agrovision-AI
AI-based plant disease and insect detection with fertilizer recommendation.
#  AgroVision AI
### AI-Based Smart Agriculture System for Plant Disease and Insect Detection

![Python](https://img.shields.io/badge/Python-3.11-blue)
![TensorFlow](https://img.shields.io/badge/TensorFlow-DeepLearning-orange)
![Flask](https://img.shields.io/badge/Flask-WebApp-black)
![HTML](https://img.shields.io/badge/Frontend-HTML%2FCSS%2FJS-green)
![Status](https://img.shields.io/badge/Status-Completed-brightgreen)

---

##  Abstract

AgroVision AI is an Artificial Intelligence-powered web application developed to assist farmers in identifying plant diseases and insect infestations from leaf images. The system uses Convolutional Neural Networks (CNN) to classify diseases and insects with high accuracy. Users simply upload an image, and the application predicts the disease or insect along with its confidence score, possible cause, and treatment recommendation.

The project aims to support smart agriculture by providing a fast, reliable, and user-friendly solution for crop health monitoring. It helps reduce crop loss through early detection and promotes the adoption of AI technologies in modern farming.

---

##  Objectives

- Detect plant diseases using Deep Learning.
- Detect common agricultural insect pests.
- Provide confidence score for predictions.
- Suggest possible treatment methods.
- Build a simple and user-friendly web application.
- Support smart farming using Artificial Intelligence.

---

##  Features

-  Plant Disease Detection
-  Insect Detection
-  Image Upload
-  CNN-based Prediction
-  Confidence Score
-  Treatment Recommendation
-  Modern Glassmorphism User Interface
-  Fast Prediction
-  Flask Web Application

---

##  Technologies Used

| Technology | Purpose |
|------------|---------|
| Python | Backend Development |
| Flask | Web Framework |
| TensorFlow / Keras | Deep Learning |
| CNN | Image Classification |
| NumPy | Data Processing |
| HTML | Structure |
| CSS | User Interface |

---

##  Project Structure

```text
AgroVision_AI_Project/
│
├── app.py
├── model/
│   ├── disease_model.h5
│   └── insect_model.h5
│
├── static/
│   ├── style.css
│   ├── anime_bg.jpg
│   ├── anime_disease.jpg
│   ├── anime_insect.jpg
│   └── uploads/
│
├── templates/
│   ├── index.html
│   └── result.html
│
├── requirements.txt
└── README.md
```
##  Download Trained Models

Due to GitHub file size limitations, the trained model files are available on Google Drive.

 **Google Drive:**  
[https://drive.google.com/drive/folders/1bBIMKEIJBg0MvIYmt22pWzYceMSfserY?usp=sharing]
---

##  Installation

Clone the repository

```bash
git clone https://github.com/dvishaal550-del/AgroVision_AI_Project.git
```

Go to the project directory

```bash
cd AgroVision_AI_Project
```

Install dependencies

```bash
pip install -r requirements.txt
```

Run the application

```bash
python app.py
```

Open your browser

```
http://127.0.0.1:5000
```

---

##  How It Works

1. Upload a plant leaf or insect image.
2. Select **Disease Detection** or **Insect Detection**.
3. The image is preprocessed.
4. The trained CNN model analyzes the image.
5. The prediction, confidence score, cause, and treatment are displayed.

---

##  Advantages

- Early disease detection
- Quick and accurate prediction
- Easy to use
- Cost-effective
- Supports precision agriculture
- Reduces crop loss

---

##  Limitations

- Depends on image quality.
- Limited to trained classes.
- Real-world performance depends on dataset diversity.

---

##  Future Scope

-  Android Mobile Application
-  Weather-based disease prediction
-  Fertilizer recommendation
-  Voice Assistant
-  Multilingual Support
-  Cloud Deployment
-  Real-time Camera Detection
-  Support for more crops and pests

---

##  Team Members

| Name | Role |
|------|------|
| **D Vishaal Nandhan** | AI Model Development & Project Lead |
| **R J Darvin Dany**| Backend Development |
| **S Anbarasu** | Frontend/UI Design |
| **G Kamalesh** | Testing & Documentation |

---

##  Acknowledgement

We sincerely thank our project guide, faculty members, and the Department of Computer Science and Engineering, University College of Engineering, Thirukkuvalai, for their continuous guidance and support. We also thank all our team members for their dedication and valuable contributions to the successful completion of AgroVision AI.

---

##  License

This project is developed for academic and educational purposes.

---

⭐ If you found this project useful, consider giving it a **Star** on GitHub!
