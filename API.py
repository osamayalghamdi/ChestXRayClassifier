from flask import Flask, request, jsonify
import os
import keras
from keras import preprocessing
import numpy as np

app = Flask(__name__)

# Load your pre-trained model
model = keras.models.load_model("D:/IT/Smesters/th5_SMESTER/305/Medi/PROJECT_TEST_NEW/PROJECT_TEST/save_model_complate.h5")

@app.route('/predict', methods=['POST'])
def predict():
    try:
        # Get the image path from the request, or use a default value
        image_path = request.json.get('image_path', 'default_image_path')
        print(image_path)
        print(image_path)


        # Check if the image file exists
        if not os.path.exists(image_path):
            return jsonify({'error': 'No image found at provided path'}), 400
        

        # Read the image file
        img = preprocessing.image.load_img(image_path, target_size=(150, 150))
        img_array = preprocessing.image.img_to_array(img)
        img_array = np.expand_dims(img_array, axis=0)
        img_array = img_array / 255.0  # Normalize the image

        # Make prediction using the loaded model
        prediction = model.predict(img_array)

        # Interpret prediction result
        predicted_class = np.argmax(prediction)
        probability = prediction[0][predicted_class]

        # Determine result label
        if predicted_class == 1:
            result = 'Pneumonia'
        else:
            result = 'NORMAL'

        # Return prediction result
        return jsonify({'result': result, 'probability': float(probability)}), 200

    except Exception as e:
        return jsonify({'error': 'Failed to process image: {}'.format(str(e))}), 500

if __name__ == '__main__':
    app.run(debug=True)




