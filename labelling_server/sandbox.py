import requests
import base64
from datetime import datetime
import shutil
from FileHelper import *

# directory = 'C:\FYP\python\\fyp_servers\labelling_server\confirmed_images\\nlb_logo'
#
# shutil.make_archive("test", 'zip', directory)
#
#
with open('test.zip', 'rb') as f:
    zipfile = base64.b64encode(f.read())

detector = 'nlb_logo'

data = {
    'detector': detector,
    'zipfile': zipfile
}

requests.post('http://127.0.0.1:5001/uploadZipFile', data=data)




# print(FileHelper.getLatestWeights("nlb_logo", "pt"))