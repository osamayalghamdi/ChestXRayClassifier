# Medical Image Analysis System

This project is a medical image analysis system that integrates a Flask-based web service with a Java application to predict pneumonia from chest X-ray images.

## Project Structure

### Python Components
- **Xproject.ipynb**: Jupyter notebook for training and testing a pneumonia detection model using TensorFlow and Keras.
- **API.py**: Flask application to serve the model and handle image prediction requests.
- **save_model_complate.h5**: Pre-trained model file.

### Java Components
- **

src

**: Java application that interacts with the Flask API and a MySQL database.
- **MediScanXNet.java**: Handles HTTP requests to the Flask API for image predictions.
- **MediScanXDB.java**: Manages database operations and user interactions.

### Database
- MySQL database for storing user data and prediction results.

## Purpose

The project aims to provide an end-to-end solution for medical image analysis, from model training to deployment and integration with a Java-based client application.
