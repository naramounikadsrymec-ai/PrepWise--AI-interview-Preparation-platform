import urllib.request
import urllib.error
import json

url = 'http://localhost:8082/api/auth/login'
headers = {'Content-Type': 'application/json'}
data = json.dumps({'email': 'testuser@example.com', 'password': 'password'}).encode('utf-8')
req = urllib.request.Request(url, data=data, headers=headers, method='POST')
try:
    with urllib.request.urlopen(req) as resp:
        print('status', resp.status)
        print(resp.read().decode())
except urllib.error.HTTPError as e:
    print('status', e.code)
    print(e.read().decode())
except Exception as e:
    print('error', e)
