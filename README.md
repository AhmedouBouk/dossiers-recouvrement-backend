# Dossiers de Recouvrement BNM - Backend

Application de gestion des dossiers de recouvrement pour la Banque Nationale de Mauritanie.

## 🚀 How to Run the Project Locally

### 🧰 Prerequisites
Make sure you have Docker and Docker Compose installed.

### 📦 Setup Steps

1. Create an empty folder (e.g., project):

mkdir project
cd project

2. Clone the frontend repository (branch prod):

git clone -b prod https://github.com/AhmedouBouk/dossier_recouvrement_frontend.git


3. Clone the backend repository (branch prod):

git clone -b prod https://github.com/AhmedouBouk/dossiers-recouvrement-backend.git


4. Navigate to the backend folder:

cd dossiers-recouvrement-backend

5. Build and start the application using Docker:

docker compose up --build


### 🌐 Access the Application
Once everything is up and running, you can test the application by visiting:

http://localhost


### 🔐 Default Admin Credentials

- Username: admin@bnm.mr
- Password: 123456
