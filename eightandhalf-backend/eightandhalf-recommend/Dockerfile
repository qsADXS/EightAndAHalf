# 使用官方 Python 运行时作为父镜像
FROM python:3.9-slim

# 设置工作目录
WORKDIR /app

# 将当前目录内容复制到容器中的 / 目录
COPY . /app
RUN pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
# 安装项目依赖
RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 9090
# 设置 PYTHONPATH
ENV PYTHONPATH=/app

# 运行 Python 脚本
CMD ["python", "main.py"]
