# Gunakan image dasar Python 3.8
FROM python:3.8-slim

# Setel direktori kerja di dalam container
WORKDIR /app

# Salin file requirements.txt ke dalam container
COPY requirements.txt /app/

# Install dependencies dari requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Salin semua file dari direktori proyek ke dalam container
COPY . /app/

# Setel variabel lingkungan untuk menjalankan Flask di Cloud Run
ENV GOOGLE_APPLICATION_CREDENTIALS="/Bangkit Dicoding\Capstone\backend-injureal\credential.json"
ENV FLASK_ENV=production
ENV PORT 8080

# Expose port yang akan digunakan untuk aplikasi
EXPOSE 8080

# Tentukan perintah untuk menjalankan aplikasi Flask
CMD ["python", "app.py"]
