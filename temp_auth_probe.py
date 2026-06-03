import urllib.request
import urllib.error

for method in ['POST']:
    print(f'=== login {method} (no Origin) ===')
    headers = {
        'Content-Type': 'application/json'
    }
    data = b'{"email":"test@example.com","password":"password"}'
    req = urllib.request.Request('http://localhost:8082/api/auth/login', data=data, headers=headers, method=method)

for method in ['OPTIONS', 'POST']:
    print(f'=== register {method} ===')
    headers = {
        'Origin': 'http://localhost:3000',
        'Content-Type': 'application/json'
    }
    if method == 'OPTIONS':
        headers['Access-Control-Request-Method'] = 'POST'
        headers['Access-Control-Request-Headers'] = 'content-type'
    data = b'{"name":"Test User","email":"testuser@example.com","password":"password"}' if method == 'POST' else None
    req = urllib.request.Request('http://localhost:8082/api/auth/register', data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read().decode('utf-8', errors='replace')
            print('status', resp.status)
            print('headers', dict(resp.getheaders()))
            print('body', repr(body))
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8', errors='replace')
        print('status', e.code)
        print('headers', dict(e.headers))
        print('body', repr(body))
    except Exception as e:
        print('error', e)
