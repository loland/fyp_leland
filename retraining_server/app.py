from flask import Flask, request
import Response
import multiprocessing
from TrainingHelper import *

app = Flask(__name__)

BASEDIR = os.getcwd()


@app.post('/uploadZipFile')
def uploadZipFile():
    jsonDict = request.form

    if 'detector' not in jsonDict or 'zipfile' not in jsonDict:
        print('request dropped')
        return Response.BAD_REQUEST

    detector: str = jsonDict['detector']
    zipfile: str = jsonDict['zipfile']


    tempTrainingDir = BASEDIR + RetrainingSettings.TRAINING_DATASET_DIR + detector + '/'
    if not os.path.isdir(tempTrainingDir):
        os.mkdir(tempTrainingDir)

    zipfilePath: str = tempTrainingDir + 'dataset.zip'
    with open(zipfilePath, 'wb') as f:
        f.write(base64.b64decode(zipfile))

    shutil.unpack_archive(zipfilePath, tempTrainingDir)
    os.remove(zipfilePath)

    TrainingHelper.modifyDataFilePaths(detector)

    # threading.Thread(target=TrainingHelper.startTraining, args=(detector,), daemon=True).start()
    multiprocessing.Process(target=TrainingHelper.startTraining, args=(detector,)).start()

    return Response.OK


if __name__ == '__main__':
    TrainingHelper.setTrainingState(False)
    app.run(port=5001)