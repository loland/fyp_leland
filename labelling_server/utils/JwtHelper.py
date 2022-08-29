import rsa
import base64
import json
import hashlib
import os
from Cryptodome.Cipher import AES
from Cryptodome.Random import get_random_bytes
from Cryptodome.Util.Padding import pad
from Cryptodome.Util.Padding import unpad

BLOCK_SIZE = 16
IV = ('A' * 16).encode()
BASEDIR = os.getcwd()

class JwtHelper:
    @staticmethod
    def generateJwtToken(username: str) -> str:
        header = {'algo': 'HS256'}
        payload = {'username': username}
        headerString = json.dumps(header)
        payloadString = json.dumps(payload)
        signature = hashlib.sha256((headerString + payloadString).encode()).hexdigest()
        token = '%s.%s.%s' % (
            JwtHelper.encryptAesThenBase64(headerString),
            JwtHelper.encryptAesThenBase64(payloadString),
            JwtHelper.encryptAesThenBase64(signature)
        )

        print('JWT token generated for user: %s' % username)
        return token

    @staticmethod
    def verifyToken(token: str) -> bool:
        components = token.split('.')
        if len(components) != 3:
            return False

        header = JwtHelper.decryptBase64ThenAes(components[0])
        payload = JwtHelper.decryptBase64ThenAes(components[1])
        signature = JwtHelper.decryptBase64ThenAes(components[2])

        if hashlib.sha256((header + payload).encode()).hexdigest() != signature:
            return False

        return True

    @staticmethod
    def getUserFromToken(token):
        components = token.split('.')
        if len(components) != 3:
            return None

        payload = JwtHelper.decryptBase64ThenAes(components[1])
        print(payload)
        return json.loads(payload)['username']


    @staticmethod
    def generateSymmetricKey():
        key = get_random_bytes(24)
        with open(BASEDIR + '/keys/symmetric.key', 'wb') as f:
            f.write(key)

    @staticmethod
    def decryptBase64ThenAes(b64String) -> str:
        bArr = base64.b64decode(b64String.encode('ascii'))
        key = JwtHelper.getSymmetricKey()
        aes = AES.new(key, AES.MODE_CBC, iv=IV)
        plaintext = unpad(aes.decrypt(bArr), BLOCK_SIZE)
        return plaintext.decode()

    @staticmethod
    def encryptAesThenBase64(string) -> str:
        key = JwtHelper.getSymmetricKey()
        aes = AES.new(key, AES.MODE_CBC, iv=IV)
        ciphertext = aes.encrypt(pad(string.encode(), BLOCK_SIZE))
        return base64.b64encode(ciphertext).decode('ascii')


    @staticmethod
    def getSymmetricKey():
        with open(BASEDIR + '/keys/symmetric.key', 'rb') as f:
            key = f.read()
            return key












    # @staticmethod
    # def generateKeys():
    #     publicKey, privateKey = rsa.newkeys(128)
    #     publicKeyString = publicKey.save_pkcs1().decode('utf8')
    #     privateKeyString = privateKey.save_pkcs1().decode('utf8')
    #
    #     with open('../keys/public.pem', 'w') as f:
    #         f.write(publicKeyString)
    #     with open('../keys/private.pem', 'w') as f:
    #         f.write(privateKeyString)
    #
    # @staticmethod
    # def getPrivateKey():
    #     with open(f'{BASEDIR}/keys/private.pem', 'rb') as f:
    #         key = f.read()
    #     return rsa.PrivateKey.load_pkcs1(key)
    #
    # @staticmethod
    # def getPublicKey():
    #     with open(f'{BASEDIR}/keys/public.pem', 'rb') as f:
    #         key = f.read()
    #     return rsa.PublicKey.load_pkcs1(key)
    #
    # @staticmethod
    # def encryptRsaThenBase64(string) -> str:
    #     stringBytes = string.encode('utf8')
    #     key = JwtHelper.getPublicKey()
    #     return base64.b64encode(rsa.encrypt(stringBytes, key)).decode('ascii')
    #
    # @staticmethod
    # def decryptBase64ThenRsa(b64String) -> str:
    #     key = JwtHelper.getPrivateKey()
    #     return rsa.decrypt(base64.b64decode(b64String), key).decode()

