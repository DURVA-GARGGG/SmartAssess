import os
import mysql.connector
from dotenv import load_dotenv

# Credentials live in backend/.env (not committed). See backend/.env.example.
load_dotenv(os.path.join(os.path.dirname(__file__), ".env"))


def get_db_connection():
    try:
        connection = mysql.connector.connect(
            host=os.getenv("DB_HOST", "localhost"),
            user=os.getenv("DB_USER", "root"),
            password=os.getenv("DB_PASSWORD", ""),
            database="smart_exam"
        )
        return connection
    except Exception as e:
        print(f"Database connection failed: {e}")
        return None
