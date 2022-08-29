import requests
import train
import export
import RetrainingSettings
import os
import shutil
import base64
import yaml
import json

BASEDIR = os.getcwd()


class TrainingHelper:
    @staticmethod
    def startTraining(detector):
        if TrainingHelper.getTrainingState():
            print("busy, training job will be postponed")
            return

        TrainingHelper.setTrainingState(True)
        print("new training job started")

        project = BASEDIR + RetrainingSettings.WEIGHTS_DIR + detector
        epochs = 10
        batch_size = 1
        data = BASEDIR + RetrainingSettings.TRAINING_DATASET_DIR + detector + '/' + RetrainingSettings.DATA_YAML_FILE
        weights = TrainingHelper.getLatestBestWeights(detector)
        img = 640

        print(
            '\n===== TRAINING PARAMETERS ====='
            '\nweights: %s'
            '\nproject: %s'
            '\nname: %s'
            '\nepochs: %s'
            '\nbatch_size: %s'
            '\ndata: %s'
            '\nimg: 640'
            '\n===============================' %
            (weights, project, detector, epochs, batch_size, data)
        )

        # run = wandb.init(reinit=True)

        try:
            if weights:
                train.run(weights=weights, project=project, name=detector, epochs=epochs, batch_size=batch_size, data=data, img=img)
            else:
                train.run(project=project, epochs=epochs, name=detector, batch_size=batch_size, data=data, img=img)

        except Exception as e:
            TrainingHelper.setTrainingState(False)
            return

        TrainingHelper.sendPtWeight(detector)

        print('TRAINING COMPLETE... EXPORTING TO TFLITE')
        TrainingHelper.setTrainingState(False)

        TrainingHelper.convertToTflite(detector)
        TrainingHelper.addTrainingToFullDataset(detector)

        print('ALL DONE... WEIGHTS SENT')
        # run.finish()


    @staticmethod
    def getLatestBestWeights(detector):
        weightsDir = BASEDIR + RetrainingSettings.WEIGHTS_DIR + detector
        # trainDir = weightsDir + '/' + detector

        weights = []
        for root, dirs, files in os.walk(weightsDir):
            for file in files:
                if file.endswith('.pt') and file != 'last.pt':
                    weights.append(root + '/' + file)

        return weights[-1]
        # if not os.path.isdir(trainDir):
        #     for root, dirs, files in os.walk(weightsDir):
        #         if len(files) == 0:
        #             return None
        #         if files[0].endswith('.pt'):
        #             return weightsDir + '/' + files[0]
        #     else:
        #         return None
        #
        # i = 1
        # while True:
        #     if not os.path.isdir(trainDir + str(i + 1)):
        #         if i == 1:
        #             return trainDir + '/weights/best.pt'
        #
        #         return trainDir + str(i) + '/weights/best.pt'
        #     i += 1

    @staticmethod
    def getLatestTfliteWeights(detector):
        weightsDir = BASEDIR + RetrainingSettings.WEIGHTS_DIR + detector
        weights = []
        for root, dirs, files in os.walk(weightsDir):
            for file in files:
                if file.endswith('.tflite'):
                    weights.append(root + '/' + file)

        if len(weights) == 0:
            return None

        return weights[-1]
        # print(weightsDir + '/' + dirs[-1] + '/weights/best-fp-16.tflite')
        # return weightsDir + '/' + dirs[-1] + '/weights/best-fp16.tflite'


    @staticmethod
    def modifyDataFilePaths(detector):
        path = BASEDIR + RetrainingSettings.TRAINING_DATASET_DIR + detector + '/'
        dataYamlPath = path + 'data.yaml'

        with open(dataYamlPath, 'r') as f:
            data: dict = yaml.safe_load(f.read())

        string = 'train: %s\nval: %s\n\nnc: %s\nnames: %s'
        with open(dataYamlPath, 'w') as f:
            f.write(string % (path + 'train', path + 'val', data['nc'], data['names']))


    @staticmethod
    def addTrainingToFullDataset(detector):
        fromPath = BASEDIR + RetrainingSettings.TRAINING_DATASET_DIR + detector + '/'
        toPath = BASEDIR + RetrainingSettings.FULL_DATASET_DIR + detector + '/'

        if not os.path.isdir(toPath):
            shutil.move(fromPath, toPath)

        else:
            trainImages = BASEDIR + RetrainingSettings.TRAINING_DATASET_DIR + detector + '/train/images/'
            trainLabels = BASEDIR + RetrainingSettings.TRAINING_DATASET_DIR + detector + '/train/labels/'
            valImages = BASEDIR + RetrainingSettings.TRAINING_DATASET_DIR + detector + '/val/images/'
            valLabels = BASEDIR + RetrainingSettings.TRAINING_DATASET_DIR + detector + '/val/labels/'

            trainImagesDest = BASEDIR + RetrainingSettings.FULL_DATASET_DIR + detector + '/train/images/'
            trainLabelsDest = BASEDIR + RetrainingSettings.FULL_DATASET_DIR + detector + '/train/labels/'
            valImagesDest = BASEDIR + RetrainingSettings.FULL_DATASET_DIR + detector + '/val/images/'
            valLabelsDest = BASEDIR + RetrainingSettings.FULL_DATASET_DIR + detector + '/val/labels/'

            for file in os.listdir(trainImages):
                shutil.move(trainImages + file, trainImagesDest + file)

            for file in os.listdir(trainLabels):
                shutil.move(trainLabels + file, trainLabelsDest + file)

            for file in os.listdir(valImages):
                shutil.move(valImages + file, valImagesDest + file)

            for file in os.listdir(valLabels):
                shutil.move(valLabels + file, valLabelsDest + file)

            shutil.rmtree(fromPath, onerror=None, ignore_errors=False)


    @staticmethod
    def convertToTflite(detector):
        weights = TrainingHelper.getLatestBestWeights(detector)
        include = ('tflite',)
        export.run(weights=weights, include=include)

        TrainingHelper.sendTfliteWeight(detector)


    @staticmethod
    def sendWeights(detector):
        TrainingHelper.sendPtWeight(detector)
        TrainingHelper.sendTfliteWeight(detector)


    @staticmethod
    def sendTfliteWeight(detector):
        tfliteWeight = TrainingHelper.getLatestTfliteWeights(detector)

        with open(tfliteWeight, 'rb') as f:
            tfliteWeightB64 = base64.b64encode(f.read()).decode('ascii')

        data = {
            'weights': tfliteWeightB64,
            'detector': detector,
            'modelType': 'tflite'
        }
        resp = requests.post(RetrainingSettings.LABELLING_SERVER_URL + RetrainingSettings.SEND_WEIGHTS_URL, data=data)

        if not (resp.status_code == 200 or resp.status_code == 201):
            print('sending tflite weights failed')
            return

        print('sending tflite weights success')


    @staticmethod
    def sendPtWeight(detector):
        ptWeight = TrainingHelper.getLatestBestWeights(detector)

        with open(ptWeight, 'rb') as f:
            ptWeightB64 = base64.b64encode(f.read()).decode('ascii')

        data = {
            'weights': ptWeightB64,
            'detector': detector,
            'modelType': 'pt'
        }
        resp = requests.post(RetrainingSettings.LABELLING_SERVER_URL + RetrainingSettings.SEND_WEIGHTS_URL, data=data)

        if not (resp.status_code == 200 or resp.status_code == 201):
            print('sending pt weights failed')
            return

        print('sending pt weights success')


    @staticmethod
    def getTrainingState():
        with open(BASEDIR + '/' + RetrainingSettings.STATE_FILE) as f:
            jsonDict = json.loads(f.read())

        if 'state' in jsonDict and jsonDict['state']:
            return True

        return False


    @staticmethod
    def setTrainingState(boolean: bool):
        with open(BASEDIR + '/' + RetrainingSettings.STATE_FILE, 'w') as f:
            json.dump({'state': boolean}, f)