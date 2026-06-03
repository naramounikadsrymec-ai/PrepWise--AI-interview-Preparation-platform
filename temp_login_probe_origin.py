import urllib.request
import urllib.error
import json

url = 'http://localhost:8082/api/auth/login'
headers = {
    'Content-Type': 'application/json',
    'Origin': 'http://localhost:3000'
}
data = json.dumps({'email': 'testuser@example.com', 'password': 'password'}).encode('utf-8')
req = urllib.request.Request(url, data=data, headers=headers, method='POST')
try:
    with urllib.request.urlopen(req) as resp:
        print('status', resp.status)
        print(resp.getheaders())
        print(resp.read().decode())
except urllib.error.HTTPError as e:
    print('status', e.code)
    print(dict(e.headers))
    print(e.read().decode())
except Exception as e:
    print('error', e)
