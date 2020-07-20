import json
import shutil

LicenseCode = 'CODE'
UserName = 'AUTHOR'

try:
    import requests
except ImportError:
    print("You need the requests library to be installed in order to use this sample.")
    print("Run 'pip install requests' to fix it.")

    exit()

RequestUrl = "http://www.ocrwebservice.com/restservices/processDocument?language=chinesesimplified,english&gettext=true"

FilePath = "FILE_PATH"

with open(FilePath, 'rb') as image_file:
    image_data = image_file.read()

r = requests.post(RequestUrl, data=image_data, auth=(UserName, LicenseCode))

if r.status_code == 401:
    print("Unauthorized request")
    exit()

jobj = json.loads(r.content)

ocrError = str(jobj["ErrorMessage"])

if ocrError != '':
    print("Recognition Error: " + ocrError)
    exit()

print("Task Description:" + str(jobj["TaskDescription"]))
print("Available Pages:" + str(jobj["AvailablePages"]))
print("Processed Pages:" + str(jobj["ProcessedPages"]))
print("Extracted Text:" + str(jobj["OCRText"][0][0]))
print("Zone 1 Page 1 Text:" + str(jobj["OCRText"][0][0]))
