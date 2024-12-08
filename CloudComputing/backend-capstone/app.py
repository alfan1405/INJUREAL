import os
import uuid
import numpy as np
from flask import Flask, request, jsonify
from google.cloud import firestore, storage
from tensorflow.keras.models import load_model
from PIL import Image
import io
import requests  # Untuk mengambil model dari URL
from dotenv import load_dotenv  # Untuk membaca file .env

# Memuat variabel lingkungan dari file .env
load_dotenv()

# Inisialisasi Flask
app = Flask(__name__)

# Konfigurasi Firebase dan Google Cloud Storage
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
BUCKET_NAME = os.getenv("BUCKET_NAME")
FIRESTORE_COLLECTION = os.getenv("FIRESTORE_COLLECTION")
MODEL_URL = os.getenv("MODEL_URL")

# Inisialisasi Firestore dan Storage
db = firestore.Client()
storage_client = storage.Client()

# Fungsi untuk memuat model dari URL
def load_model_from_url(model_url):
    try:
        # Ambil file model dari URL
        response = requests.get(model_url)
        response.raise_for_status()
        with open("temp_model.h5", "wb") as f:
            f.write(response.content)
        print("Model downloaded successfully from URL.")
        return load_model("temp_model.h5")
    except Exception as e:
        print(f"Error loading model from URL: {e}")
        raise e

# Muat model machine learning
model = load_model_from_url(MODEL_URL)

# Daftar kelas, panduan penanganan, dan rekomendasi obat
class_labels = {
    "Abrasions": {
        "description": "Luka lecet akibat gesekan dengan permukaan kasar.",
        "steps": ["Bersihkan luka dengan air mengalir", "Oleskan antiseptik", "Balut dengan perban"],
        "warnings": ["Jangan gunakan alkohol langsung pada luka terbuka"],
        "do_not": ["Menggosok luka dengan kain kasar"],
        "recommended_medicines": ["Salep antiseptik", "Betadine"],
    },
    "Bruises": {
        "description": "Luka memar akibat benturan keras yang merusak pembuluh darah kecil.",
        "steps": ["Kompres dengan es selama 15 menit", "Hindari tekanan pada area yang memar"],
        "warnings": ["Jangan tekan terlalu keras pada area memar"],
        "do_not": ["Menggunakan obat tanpa konsultasi dokter"],
        "recommended_medicines": ["Salep Arnica", "Krim anti-inflamasi"],
    },
    "Burns": {
        "description": "Luka bakar akibat panas, kimia, atau listrik.",
        "steps": ["Alirkan air dingin ke area luka bakar selama 10 menit", "Gunakan salep khusus luka bakar"],
        "warnings": ["Jangan pecahkan lepuh luka bakar"],
        "do_not": ["Mengoleskan mentega atau minyak"],
        "recommended_medicines": ["Salep Silver Sulfadiazine", "Krim lidah buaya"],
    },
}

@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'No file part provided.'}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No selected file.'}), 400

    try:
        # Validasi file gambar
        try:
            img = Image.open(file)
            img.verify()  # Validasi gambar
            file.seek(0)  # Reset posisi file setelah validasi
        except Exception as e:
            return jsonify({'error': f'Invalid image file: {str(e)}'}), 400

        # Unggah gambar ke Google Cloud Storage
        file_extension = file.filename.rsplit('.', 1)[1].lower()
        unique_filename = f"{uuid.uuid4()}.{file_extension}"
        bucket = storage_client.bucket(BUCKET_NAME)
        blob = bucket.blob(unique_filename)
        blob.upload_from_file(file)
        image_url = f"https://storage.googleapis.com/{BUCKET_NAME}/{unique_filename}"

        # Proses gambar untuk prediksi
        file.seek(0)  # Reset file untuk dibaca kembali
        img = Image.open(file)
        img = img.resize((150, 150))  # Ukuran sesuai input model
        img_array = np.array(img) / 255.0  # Normalisasi gambar
        img_array = np.expand_dims(img_array, axis=0)

        # Prediksi dengan model
        predictions = model.predict(img_array)
        predicted_class = np.argmax(predictions[0])
        predicted_label = list(class_labels.keys())[predicted_class]
        treatment_data = class_labels[predicted_label]

        # Simpan data ke Firestore
        data_to_save = {
            "imageUrl": image_url,
            "wounds_name": predicted_label,
            "description": treatment_data["description"],
            "treatment": treatment_data["steps"],
            "warnings": treatment_data["warnings"],
            "do_not": treatment_data["do_not"],
            "recommended_medicines": treatment_data["recommended_medicines"],
            "user_id": request.form.get("user_id", "unknown"),  # Ambil user_id dari form
            "timestamp": firestore.SERVER_TIMESTAMP,
        }
        db.collection(FIRESTORE_COLLECTION).add(data_to_save)

        # Kirim respons ke klien
        return jsonify({
            "prediction": predicted_label,
            "description": treatment_data["description"],
            "treatment": treatment_data["steps"],
            "warnings": treatment_data["warnings"],
            "do_not": treatment_data["do_not"],
            "recommended_medicines": treatment_data["recommended_medicines"],
            "imageUrl": image_url,
        }), 200

    except Exception as e:
        return jsonify({'error': f'Internal server error: {str(e)}'}), 500

if __name__ == '__main__':
    # Menggunakan PORT environment variable yang disediakan oleh Cloud Run
    import os
    port = int(os.environ.get('PORT', 8080))
    app.run(host='0.0.0.0', port=port)
