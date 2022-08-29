import json
import cv2
import Settings
import os
from FileHelper import *

BASEDIR = os.getcwd()
class BoundingBoxHelper:
    @staticmethod
    def getCv2Image(guid):
        filepath = BASEDIR + Settings.UNCONFIRMED_IMAGES_DIRECTORY + guid + '.jpg'
        return cv2.imread(filepath)

    @staticmethod
    def getImageBytesWithBoundingBoxes(guid):
        image = BoundingBoxHelper.getCv2Image(guid)

        if type(image) != np.ndarray:
            FileHelper.removeImageEntry(guid)
            return None

        detectorName = FileHelper.getImageAttributes(guid)[0]
        colors = Settings.DETECTOR_COLOR_MAPPING[detectorName]

        boxes: list = FileHelper.getBoundingBoxString(guid).split('|')
        for box in boxes:
            temp = box.split(',')

            if len(temp[0]) == 0:
                FileHelper.removeImage(guid)
                FileHelper.removeImageEntry(guid)

            detectedClass = int(temp[0])
            width = float(temp[1])
            height = float(temp[2])
            x = int(float(temp[3]))
            y = int(float(temp[4]))

            halfWidth = int(width / 2)
            halfHeight = int(height / 2)

            startPoint = (x - halfWidth, y - halfHeight)
            endPoint = (x + halfWidth, y + halfHeight)
            color = colors[detectedClass]
            color = BoundingBoxHelper.rgbToBgr(color)
            thickness = 2

            image = cv2.rectangle(image, startPoint, endPoint, color, thickness)

        return cv2.imencode('.jpg', image)[1].tobytes()


    @staticmethod
    def rgbToBgr(rgb: list):
        return rgb[2], rgb[1], rgb[0]