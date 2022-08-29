from flask import Flask, request, make_response, render_template, redirect, url_for, jsonify
import base64
import uuid
from FileHelper import *
import traceback
import Response
import os
from JwtHelper import *
import cv2
from BoundingBoxHelper import *
from FaceBlurringHelper import *

app = Flask(__name__)

BASEDIR = os.getcwd()

@app.route("/")
def home():
    token = getToken(request)
    if not token:
        url = request.base_url.replace('http:', 'https:') + 'login'
        return redirect(url)

    username = JwtHelper.getUserFromToken(token)
    if not username:
        url = request.base_url.replace('http:', 'https:') + 'login'
        return redirect(url)

    return render_template('home.html', username=username)


@app.route("/sendImage", methods=['POST'])
def sendImage():
    jsonString = request.data.decode()
    try:
        jsonDict = json.loads(jsonString)
        print("received")
    except ValueError:
        traceback.print_exc()
        return jsonify({'success': False}), 400

    guid = str(uuid.uuid4())
    detectorName = jsonDict['detector']
    imageWidth = jsonDict['imageWidth']
    imageHeight = jsonDict['imageHeight']

    boundingBoxString = jsonDict['boundingBoxes']
    if len(boundingBoxString) == 0:
        print('dropping image, no bounding boxes')
        return Response.BAD_REQUEST

    FileHelper.addImageEntry(guid, detectorName, imageWidth, imageHeight, boundingBoxString)

    filename = guid + '.jpg'
    FileHelper.addImage(jsonDict['image'], filename)

    return Response.OK


@app.get('/getImage')
def getImage():
    token: str = getToken(request)
    if not token:
        return jsonify({'success': False, 'resp': 'Token not found'})

    username: str = JwtHelper.getUserFromToken(token)
    print('fetching image for user: ' + username)

    while True:
        imageGuid: str = FileHelper.getImageGuid(username)
        if not imageGuid:
            return jsonify({'success': False, 'resp': 'No images available'})

        imageBytes: bytes = BoundingBoxHelper.getImageBytesWithBoundingBoxes(imageGuid)
        if imageBytes:
            break

    detectorName = FileHelper.getImageAttributes(imageGuid)[0]

    colors = Settings.DETECTOR_COLOR_MAPPING[detectorName]
    classes = Settings.DETECTOR_CLASS_MAPPING[detectorName]

    return jsonify({
        'success': True,
        'guid': imageGuid,
        'detectorName': Settings.DETECTOR_NAME_MAPPING[detectorName],
        'colors': {classes[i]: str(colors[i]) for i in range(len(colors))},
        'imageBytes': base64.b64encode(imageBytes).decode('ascii')
    }), 200



@app.post('/validateImage')
def validateImage():
    if len(request.data) == 0:
        return Response.BAD_REQUEST

    token: str = getToken(request)
    if not token:
        return jsonify({'success': False, 'resp': 'Token not found'})
    username: str = JwtHelper.getUserFromToken(token)

    jsonString: str = request.data.decode()
    jsonDict: dict = json.loads(jsonString)

    guid: str = jsonDict['guid']
    correct: bool = jsonDict['correct']

    FileHelper.addImageValidityCounter(guid, correct, username)
    return Response.OK



@app.post('/login')
def authenticate():
    if len(request.data) == 0:
        return Response.BAD_REQUEST

    jsonString: str = request.data.decode()
    jsonDict: dict = json.loads(jsonString)
    username: str = jsonDict['username']
    password: str = jsonDict['password']

    if len(username) == 0 or len(password) == 0:
        return Response.BAD_REQUEST

    if FileHelper.checkCredentials(username, password):
        token: str = JwtHelper.generateJwtToken(username)
        resp = make_response(jsonify({'success': True, 'token': token}), 200)
        resp.set_cookie('token', token)
        return resp

    return jsonify({'success': False, 'resp': 'Invalid credentials'}), 401



@app.post('/register')
def register():
    if len(request.data) == 0:
        return Response.BAD_REQUEST

    jsonString: str = request.data.decode()
    jsonDict: dict = json.loads(jsonString)
    username: str = jsonDict['username']
    password: str = jsonDict['password']

    if not username.isalnum():
        return jsonify({'success': False, 'resp': 'Only alphanumeric characters in username'}), 403

    if FileHelper.checkIfUserExists(username):
        return jsonify({'success': False, 'resp': 'Username exists'}), 409

    FileHelper.addUser(username, password)
    return jsonify({'success': True, 'token': JwtHelper.generateJwtToken(username)}), 201


@app.get('/register')
def page_register():
    return render_template('register.html')


@app.get('/login')
def page_login():
    return render_template('login.html')


@app.post('/sendWeights')
def sendWeights():
    jsonDict = request.form
    if 'weights' not in jsonDict:
        return Response.BAD_REQUEST

    weightsB64: str = jsonDict['weights']
    detector: str = jsonDict['detector']
    modelType: str = jsonDict['modelType']

    FileHelper.storeWeights(detector, weightsB64, modelType)
    return Response.OK


# NEED TO RETURN WEIGHTS ONE AT A TIME
# @app.get('/getWeights')
# def getWeights():
#     if not ('modelType' in request.args and 'detectors' in request.args):
#         return Response.BAD_REQUEST
#
#     # {
#     #     'detectors': {
#     #         'red_green_man': timestamp,
#     #         'bus_stop': timestamp
#     #     }
#     #     'modelType': 'tflite/pt'
#     # }
#
#     modelType = request.args.get('modelType')
#     detectors: dict = json.loads(request.args.get('detectors'))
#
#     weightsDict: dict = {}
#     for detector in detectors:
#
#         timestamp = str(detectors[detector])
#         existingWeights = FileHelper.listWeightsFile(detector)
#
#         for weight in existingWeights:
#             weightName = weight.split('/')[-1]
#             weightTimestamp, weightType = weightName.split('.')
#
#             if weightType != modelType:
#                 continue
#
#             if weightTimestamp == timestamp:
#                 break
#
#             with open(weight, 'rb') as f:
#                 weightB64 = base64.b64encode(f.read()).decode('ascii')
#
#             weightsDict[detector] = {
#                 'weight': weightB64,
#                 'timestamp': weightTimestamp
#             }
#             break
#
#     if len(weightsDict) == 0:
#         return json.dumps({'success': False, 'resp': 'no weights found'}), 404
#
#     respDict = {
#         'success': True,
#         'weights': weightsDict
#     }
#
#     print('returned: ', [weight for weight in weightsDict])
#
#     return json.dumps(respDict), 200


@app.get('/getWeights')
def getWeights():
    if not ('modelType' in request.args and 'detectors' in request.args):
        return Response.BAD_REQUEST

    # {
    #     'detectors': {
    #         'red_green_man': timestamp,
    #         'bus_stop': timestamp
    #     }
    #     'modelType': 'tflite/pt'
    # }

    respDict = {
        'success': True,
        'detectors': {}
    }

    modelType = request.args.get('modelType')
    detectors: dict = json.loads(request.args.get('detectors'))

    for detector in detectors:

        timestamp = str(detectors[detector])
        existingWeights = FileHelper.listWeightsFile(detector)

        for weight in existingWeights:
            weightName = weight.split('/')[-1]
            weightTimestamp, weightType = weightName.split('.')

            if weightType != modelType:
                continue

            if weightTimestamp == timestamp:
                break

            respDict['detectors'][detector] = weightName
            break


    if len(respDict['detectors']) == 0:
        return json.dumps({'success': False, 'resp': 'no weights found'}), 404

    print('returned: ', respDict)
    return json.dumps(respDict), 200



@app.get('/getWeight')
def getWeight():
    if not ('modelType' in request.args and 'detector' in request.args):
        return Response.BAD_REQUEST

    modelType = request.args.get('modelType')
    detector = request.args.get('detector')

    weight = FileHelper.getLatestWeights(detector, modelType)
    timestamp = weight.split('/')[-1].split('.')[0]

    with open(weight, 'rb') as f:
        weightB64 = base64.b64encode(f.read()).decode('ascii')

    return json.dumps({"success": True, "weight": weightB64, "timestamp": timestamp})



def getToken(req):
    token = None
    if 'token' in req.cookies:
        token = req.cookies.get('token')
        print('token found in cookie: ' + token)

    elif 'Authorization' in req.headers:
        token = req.headers.get("Authorization")
        print('token found in header: ' + token)

    return token


if __name__ == "__main__":
    app.run(debug=True)