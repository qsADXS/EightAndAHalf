import logging

def setup_logger(name, log_file="log", level=logging.INFO):
    """配置和创建日志记录器"""
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

    # 创建文件处理器，将日志输出到文件
    file_handler = logging.FileHandler(log_file)
    file_handler.setFormatter(formatter)

    # 创建控制台处理器，将日志输出到控制台
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)

    return logger