import cv2
import numpy as np
import os
import Settings

BASEDIR = os.getcwd()
BASEDIR = BASEDIR.replace('\\', '/')

# print(BASEDIR + Settings.BLURRING_DIRECTORY + 'deploy.prototxt.txt')
prototxt_path = BASEDIR + Settings.BLURRING_DIRECTORY + 'deploy.prototxt.txt'
model_path = BASEDIR + Settings.BLURRING_DIRECTORY + "res10_300x300_ssd_iter_140000_fp16.caffemodel"
model = cv2.dnn.readNetFromCaffe(prototxt_path, model_path)

class FaceBlurringHelper:
    @staticmethod
    def blurImage(imageBytes):

        jpgArr = np.frombuffer(imageBytes, dtype=np.uint8)
        image = cv2.imdecode(jpgArr, flags=1)

        h, w = image.shape[:2]
        # gaussian blur kernel size depends on width and height of original image
        kernel_width = (w // 7) | 1
        kernel_height = (h // 7) | 1
        # preprocess the image: resize and performs mean subtraction
        blob = cv2.dnn.blobFromImage(image, 1.0, (300, 300), (104.0, 177.0, 123.0))
        # set the image into the input of the neural network
        model.setInput(blob)
        # perform inference and get the result
        output = np.squeeze(model.forward())

        for i in range(0, output.shape[0]):
            confidence = output[i, 2]
            if confidence > 0.2:
                box = output[i, 3:7] * np.array([w, h, w, h])
                start_x, start_y, end_x, end_y = box.astype(int)
                face = image[start_y: end_y, start_x: end_x]
                face = cv2.GaussianBlur(face, (kernel_width, kernel_height), 0)
                image[start_y: end_y, start_x: end_x] = face

        return cv2.imencode('.jpg', image)[1].tobytes()