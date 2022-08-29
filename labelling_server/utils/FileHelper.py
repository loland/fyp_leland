import json
import Settings
import traceback
import hashlib
import base64
import os
import cv2
from datetime import datetime
import requests
import threading
import shutil
import random
from FaceBlurringHelper import *

BASEDIR = os.getcwd()


class FileHelper:
    @staticmethod
    def addImageEntry(guid, detectorName, imageWidth, imageHeight, boundingBoxString):
        print('addImageEntry: ' + guid)
        imageDict = FileHelper.readImageDatabase()
        imageDict[guid] = {
            'attr': '%s,%s,%s' % (detectorName, imageWidth, imageHeight),
            'boundingBoxString': boundingBoxString,
            'usernames': '',
            'yes': 0,
            'no': 0
        }
        FileHelper.writeImageDatabase(imageDict)

    @staticmethod
    def getImageGuid(username):
        with open(BASEDIR + Settings.IMAGE_DATABASE, 'r') as f:
            imageDict: dict = json.loads(f.read())

        if len(imageDict) == 0:
            return None

        for guid in imageDict:
            usernamesString: str = imageDict[guid]['usernames']
            usernames: list = usernamesString.split(',')
            if username in usernames:
                continue
            return guid

        return None

    @staticmethod
    def addImageValidityCounter(guid: str, correct: bool, username: str):
        imageDict: dict = FileHelper.readImageDatabase()

        if guid not in imageDict:
            return

        usernames = imageDict[guid]['usernames']
        if username in usernames.split(','):
            return

        if len(usernames) == 0:
            imageDict[guid]['usernames'] = username
        else:
            imageDict[guid]['usernames'] = usernames + ',' + username


        if correct:
            imageDict[guid]['yes'] += 1
            # if yesCount hits threshold, confirm the image
            if imageDict[guid]['yes'] >= Settings.YES_COUNT_THRESHOLD:
                print("confirmed image: " + guid)
                FileHelper.confirmImage(guid)
                del imageDict[guid]

        else:
            imageDict[guid]['no'] += 1
            # if noCount hits threshold, remove the image and image entry
            if imageDict[guid]['no'] >= Settings.NO_COUNT_THRESHOLD:
                print("removing image: " + guid)
                FileHelper.removeImage(guid)
                del imageDict[guid]

        FileHelper.writeImageDatabase(imageDict)

    @staticmethod
    def confirmImage(guid):
        fromPath: str = BASEDIR + Settings.UNCONFIRMED_IMAGES_DIRECTORY + guid + '.jpg'
        detectorName = FileHelper.getImageAttributes(guid)[0]

        detectorDir = BASEDIR + Settings.CONFIRMED_IMAGES_DIRECTORY + detectorName

        if not os.path.isdir(detectorDir):
            os.mkdir(detectorDir)

            trainDir = detectorDir + Settings.RELATIVE_TRAIN_DIRECTORY[0:-1]
            valDir = detectorDir + Settings.RELATIVE_VAL_DIRECTORY[0:-1]

            os.mkdir(trainDir)
            os.mkdir(trainDir + Settings.RELATIVE_IMAGE_DIRECTORY[0:-1])
            os.mkdir(trainDir + Settings.RELATIVE_LABEL_DIRECTORY[0:-1])

            os.mkdir(valDir)
            os.mkdir(valDir + Settings.RELATIVE_IMAGE_DIRECTORY[0:-1])
            os.mkdir(valDir + Settings.RELATIVE_LABEL_DIRECTORY[0:-1])

            detectorName = FileHelper.getImageAttributes(guid)[0]
            classes = Settings.DETECTOR_CLASS_MAPPING[detectorName]
            dataYamlString = 'train: %s\nval: %s\n\nnc: %s\nnames: %s' % (trainDir, valDir, len(classes), str(list(classes)))
            with open(detectorDir + '/' + Settings.DATA_YAML_FILE, 'w') as f:
                f.write(dataYamlString)


        if random.random() <= Settings.VAL_CHANCE:
            toPath: str = detectorDir + Settings.RELATIVE_VAL_DIRECTORY + Settings.RELATIVE_IMAGE_DIRECTORY + guid + '.jpg'
            labelPath: str = detectorDir + Settings.RELATIVE_VAL_DIRECTORY + Settings.RELATIVE_LABEL_DIRECTORY + guid + '.txt'
        else:
            toPath: str = detectorDir + Settings.RELATIVE_TRAIN_DIRECTORY + Settings.RELATIVE_IMAGE_DIRECTORY + guid + '.jpg'
            labelPath: str = detectorDir + Settings.RELATIVE_TRAIN_DIRECTORY + Settings.RELATIVE_LABEL_DIRECTORY + guid + '.txt'

        shutil.move(fromPath, toPath)

        labels: str = FileHelper.getBoundingBoxLabelString(guid)
        with open(labelPath, 'w') as f:
            f.write(labels)


        imageDir = detectorDir + Settings.RELATIVE_TRAIN_DIRECTORY + Settings.RELATIVE_IMAGE_DIRECTORY

        i = 0
        for root, dirs, files in os.walk(imageDir):
            for i in range(len(files)):
                i += 1

        print(str(i) + ' images in test set')

        if i >= Settings.RETRAINING_COUNT_THRESHOLD:
            threading.Thread(target=FileHelper.sendRetrainingFiles, args=(detectorName,)).start()



    @staticmethod
    def getImageAttributes(guid) -> list:
        imageDict = FileHelper.readImageDatabase()
        return imageDict[guid]['attr'].split(',')

    @staticmethod
    def getImageBytes(guid):
        filepath: str = BASEDIR + Settings.UNCONFIRMED_IMAGES_DIRECTORY + guid + '.jpg'
        try:
            with open(filepath, 'rb') as f:
                return f.read()
        except FileNotFoundError:
            imageDict = FileHelper.readImageDatabase()
            del imageDict[guid]
            FileHelper.writeImageDatabase(imageDict)

    @staticmethod
    def readImageDatabase() -> dict:
        try:
            with open(BASEDIR + Settings.IMAGE_DATABASE, 'r') as f:
                imageDict: dict = json.loads(f.read())
                return imageDict
        except FileNotFoundError or json.decoder.JSONDecodeError as e:
            imageDict = {}
            with open(BASEDIR + Settings.IMAGE_DATABASE, 'w') as f:
                json.dump(imageDict, f, indent=4)
                return imageDict

    @staticmethod
    def writeImageDatabase(imageDict: dict):
        with open(BASEDIR + Settings.IMAGE_DATABASE, 'w') as f:
            json.dump(imageDict, f, indent=4)

    # NOT REMOVING PROPERLY
    @staticmethod
    def removeImageEntry(guid):
        imageDict = FileHelper.readImageDatabase()
        del imageDict[guid]
        print(guid, [guid for guid in imageDict])
        FileHelper.writeImageDatabase(imageDict)
        print('removed image entry: ' + guid)

    @staticmethod
    def removeImage(guid):
        filepath = BASEDIR + Settings.UNCONFIRMED_IMAGES_DIRECTORY + guid + '.jpg'
        print('removed image: ' + guid)
        os.remove(filepath)

    @staticmethod
    def addImage(base64String, filename):
        filepath = BASEDIR + Settings.UNCONFIRMED_IMAGES_DIRECTORY + filename
        imageBytes = base64.b64decode(base64String)
        imageBytes = FaceBlurringHelper.blurImage(imageBytes)

        if not os.path.isdir(BASEDIR + Settings.UNCONFIRMED_IMAGES_DIRECTORY):
            os.mkdir(BASEDIR + Settings.UNCONFIRMED_IMAGES_DIRECTORY)

        with open(filepath, 'wb') as f:
            f.write(imageBytes)

    @staticmethod
    def addUser(username, password):
        try:
            with open(BASEDIR + Settings.USER_DATABASE, 'r') as f:
                userDict = json.loads(f.read())
        except ValueError:
            print('creating new user json file')
            userDict = {}

        userDict[username] = hashlib.sha256(password.encode()).hexdigest()
        with open(BASEDIR + Settings.USER_DATABASE, 'w') as f:
            json.dump(userDict, f, indent=4)

    @staticmethod
    def checkIfUserExists(username):
        try:
            with open(BASEDIR + Settings.USER_DATABASE, 'r') as f:
                userDict = json.loads(f.read())
        except ValueError:
            return False

        except FileNotFoundError:
            with open(BASEDIR + Settings.USER_DATABASE, 'w') as f:
                f.write("{}")
            return False

        if username in userDict:
            return True

        return False

    @staticmethod
    def checkCredentials(username, password):
        try:
            with open(BASEDIR + Settings.USER_DATABASE, 'r') as f:
                userDict = json.loads(f.read())
        except FileNotFoundError or json.decoder.JSONDecodeError:
            with open(BASEDIR + Settings.USER_DATABASE, 'w') as f:
                f.write("{}")
            return False

        if username in userDict and userDict[username] == hashlib.sha256(password.encode()).hexdigest():
            return True

        return False

    @staticmethod
    def decodeBoundingBoxString(boundingBoxString):
        boxes = boundingBoxString.split('|')
        for box in boxes:
            temp = box.split(',')
            detectedClass = temp[0]
            width = temp[1]
            height = temp[2]
            x = temp[3]
            y = temp[4]

    # Bounding Box static methods placed here to avoid circular import errors
    @staticmethod
    def getBoundingBoxString(guid):
        imageDict = FileHelper.readImageDatabase()
        return imageDict[guid]['boundingBoxString']

    @staticmethod
    def getBoundingBoxLabelString(guid) -> str:
        detectorName, imageWidth, imageHeight = FileHelper.getImageAttributes(guid)
        imageWidth = int(imageWidth)
        imageHeight = int(imageHeight)

        boxes: list = FileHelper.getBoundingBoxString(guid).split('|')

        entries = []
        for box in boxes:
            temp = box.split(',')
            detectedClass: int = int(temp[0])
            width: float = float(temp[1]) / imageWidth
            height: float = float(temp[2]) / imageHeight
            x: float = float(temp[3]) / imageWidth
            y: float = float(temp[4]) / imageHeight

            entries.append('%s %s %s %s %s' % (detectedClass, x, y, width, height))

        return '\n'.join(entries)


    @staticmethod
    def sendRetrainingFiles(detector) -> bool:
        directory = BASEDIR + Settings.CONFIRMED_IMAGES_DIRECTORY + detector

        tempFileName = BASEDIR + Settings.TEMP_DIR + 'temp.zip'
        shutil.make_archive(BASEDIR + Settings.TEMP_DIR + 'temp', 'zip', directory)

        with open(tempFileName, 'rb') as f:
            zipfile = base64.b64encode(f.read())

        os.remove(tempFileName)

        data = {
            'detector': detector,
            'zipfile': zipfile
        }

        try:
            resp = requests.post('http://127.0.0.1:5001/uploadZipFile', data=data)
            if resp.status_code == 200 or resp.status_code == 201:
                print('successfully sent images to retraining server')
                detectorDir = BASEDIR + Settings.CONFIRMED_IMAGES_DIRECTORY + detector
                shutil.rmtree(detectorDir, onerror=None, ignore_errors=False)
                return True

            print('Failed to send images to retraining server')
            return False

        except requests.exceptions.ConnectionError:
            print("unable to reach retraining server")




    @staticmethod
    def storeWeights(detector, weightsB64, modelType):
        weightsDir = BASEDIR + Settings.WEIGHTS_DIR
        if not os.path.isdir(weightsDir):
            os.mkdir(weightsDir)

        detectorWeightsDir = weightsDir + detector + '/'
        if not os.path.isdir(detectorWeightsDir):
            os.mkdir(detectorWeightsDir)

        timestamp = str(int(datetime.timestamp(datetime.now())))

        with open(detectorWeightsDir + timestamp + '.' + modelType, 'wb') as f:
            f.write(base64.b64decode(weightsB64))

    @staticmethod
    def listWeightsFile(detector):
        detectorWeightsDir = BASEDIR + Settings.WEIGHTS_DIR + detector
        if not os.path.isdir(detectorWeightsDir):
            return []

        temp = []
        for root, dirs, files in os.walk(detectorWeightsDir):
            for file in files[::-1]:
                temp.append(root + "/" + file)

        return temp


    @staticmethod
    def getLatestWeights(detector, modelType):
        existingWeights = FileHelper.listWeightsFile(detector)
        for weight in existingWeights:
            if weight.endswith("." + modelType):
                return weight

        return None


