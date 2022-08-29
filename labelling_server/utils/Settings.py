UNCONFIRMED_IMAGES_DIRECTORY = '/unconfirmed_images/'
CONFIRMED_IMAGES_DIRECTORY = '/confirmed_images/'

RELATIVE_TRAIN_DIRECTORY = '/train/'
RELATIVE_VAL_DIRECTORY = '/val/'
RELATIVE_LABEL_DIRECTORY = '/labels/'
RELATIVE_IMAGE_DIRECTORY = '/images/'

TEMP_DIR = '/temp/'

BLURRING_DIRECTORY = '/blurring/'

WEIGHTS_DIR = '/weights/'

IMAGE_DATABASE = '/databases/images.json'
USER_DATABASE = '/databases/users.json'
YES_COUNT_THRESHOLD = 1
NO_COUNT_THRESHOLD = 1
DATA_YAML_FILE = 'data.yaml'
VAL_CHANCE = 0.2

RETRAINING_COUNT_THRESHOLD = 10
RETRAINING_SERVER_URL = '127.0.0.1:5001'

DETECTOR_NAME_MAPPING = {
    'nlb_logo': 'NLB Logo',
    'red_green_man': 'Traffic Lights',
    'bus_stop': 'Bus Stop'
}

DETECTOR_COLOR_MAPPING = {
    'nlb_logo': ((80, 124, 252),),
    'red_green_man': ((88, 164, 76), (200, 60, 76)),
    'bus_stop': ((88, 164, 76), (200, 60, 76), (80, 124, 252))
}

DETECTOR_CLASS_MAPPING = {
    'nlb_logo': ('nlb-logo',),
    'red_green_man': ('green-man', 'red-man'),
    'bus_stop': ('horizontal-sign', 'pillar', 'vertical-sign')
}