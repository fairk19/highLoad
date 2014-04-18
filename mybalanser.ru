upstream backend {
    server 127.0.0.1:8081 weight=10 max_fails=3 fail_timeout=150;
    server 127.0.0.1:8082 weight=10 max_fails=3 fail_timeout=150;
    server 127.0.0.1:8083 weight=10 max_fails=3 fail_timeout=150;
    server 127.0.0.1:8084 weight=1 backup;
}

server {
    listen    80;

    location ~* \.()$ {
    root   /home/alexandr/highLoad;  }

    location / {
    client_max_body_size    10m;
    client_body_buffer_size 128k;
    proxy_send_timeout   90;
    proxy_read_timeout   90;
    proxy_buffer_size    4k;
    proxy_buffers     16 32k;
    proxy_busy_buffers_size 64k;
    proxy_temp_file_write_size 64k;
    proxy_connect_timeout 30s;
    proxy_pass   http://backend;
    proxy_set_header   Host   $host;
    proxy_set_header   X-Real-IP  $remote_addr;
    proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
   }

	location ~* /.(jpg|jpeg|gif|png|css|mp3|avi|mpg|txt|js|jar|rar|zip|tar|wav|wmv)$ {
	root    /home/alexandr/highLoad;}

 }
