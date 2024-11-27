import pika
import json

credentials = pika.PlainCredentials('linbei', '888888')
connection = pika.BlockingConnection(pika.ConnectionParameters(
    host='106.54.22.249',
    credentials=credentials
))
channel = connection.channel()
channel.queue_declare(queue='czhyyds')

def callback(ch, method, properties, body):
    print('读取到消息')
    try:
        message_str = body.decode('utf-8')
        print(message_str)
        message_dict = json.loads(message_str)
        url = message_dict.get('url')
        id = message_dict.get('id')
        print(f"Received message: url={url}, id={id}")
    except NameError:
        print("NameError")
    finally:
        print("Closing connection")
        print('等待消息中...')

channel.basic_consume(queue='czhyyds', on_message_callback=callback, auto_ack=True)

print('等待消息中...')

channel.start_consuming()
