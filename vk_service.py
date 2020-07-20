import requests
import vk_api
from vk_api import VkUpload
from vk_api.longpoll import VkLongPoll, VkEventType
import yaml
from datetime import datetime

from vk_api.utils import get_random_id

session = requests.Session()

with open(r'config.yaml') as stream:
    vk_session = vk_api.VkApi(token=yaml.load(stream, Loader=yaml.FullLoader).get("vk_token"))

    upload = VkUpload(vk_session)
    longpoll = VkLongPoll(vk_session)

    vk = vk_session.get_api()

    for event in longpoll.listen():
        if event.type == VkEventType.MESSAGE_NEW:
            vk.messages.send(
                user_id=event.user_id,
                random_id=get_random_id(),
                message='No results'
            )
            print('Сообщение пришло в: ' + str(datetime.strftime(datetime.now(), "%H:%M:%S")))
            print('Текст сообщения: ' + str(event.text))
