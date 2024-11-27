from server.Server import serve
from config.logger_config import setup_logger
logger = setup_logger(__name__)
logger.info("开始运行")
serve()