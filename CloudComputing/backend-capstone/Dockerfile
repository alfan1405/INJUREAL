# Menggunakan image Python
FROM python:3.9-slim

# Menetapkan direktori kerja
WORKDIR /app

# Menyalin file requirements.txt dan menginstal dependensi
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

# Menyalin sisa file aplikasi
COPY . /app/

# Mengatur variabel lingkungan PORT
ENV PORT=8080

# Menjalankan aplikasi Flask
CMD ["python", "app.py"]
