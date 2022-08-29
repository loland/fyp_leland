import json

OK = json.dumps({'success': True}), 200
BAD_REQUEST = json.dumps({'success': False}), 400

