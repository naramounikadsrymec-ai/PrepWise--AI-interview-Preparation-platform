# PrepWise

## Quick start

### Requirements
- Java 17 installed and available on `PATH`
- Node.js and npm installed
- A terminal opened at the repository root (`c:\Project\PrepWise`)

### Start both backend and frontend
1. Open PowerShell at `c:\Project\PrepWise`
2. Run:
   ```powershell
   .\run.ps1
   ```

### Start only backend
```powershell
.\run-backend.ps1
```

### Start only frontend
```powershell
.\run-frontend.ps1
```

### Alternative Windows command prompt launch
```cmd
run.cmd
```

### Notes
- The backend uses the Maven wrapper at `backend\backend\mvnw.cmd`.
- The frontend runs from `frontend` and installs `node_modules` automatically if needed.
- Backend default port: `8080`
- Frontend default port: `3000`
