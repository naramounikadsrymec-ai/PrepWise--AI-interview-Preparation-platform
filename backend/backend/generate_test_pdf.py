from pathlib import Path
pdf = b"%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n4 0 obj\n<< /Length 107 >>\nstream\nBT /F1 24 Tf 100 740 Td (John Doe) Tj ET BT /F1 14 Tf 100 720 Td (Software Engineer) Tj ET BT /F1 12 Tf 100 700 Td (Skills: Java, Spring, SQL) Tj ET\nendstream\nendobj\n5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\nxref\n0 6\n0000000000 65535 f \n0000000010 00000 n \n0000000074 00000 n \n0000000132 00000 n \n0000000271 00000 n \n0000000382 00000 n \ntrailer\n<< /Root 1 0 R /Size 6 >>\nstartxref\n451\n%%EOF\n"
path = Path('resume_test.pdf')
path.write_bytes(pdf)
print('created', path.exists())
