**Requirements**

Установить terraform, terragrunt, helm, kubectl, idea, jdk11, docker, gradle 7+.

Настроить maven:
settings.xml скопировать в ~/.m2 и выставить пароли

Скачать teraform.d: https://cloud.mail.ru/public/9V9u/zKDYqKbHw

Инициализировать провайдеров в каждом модуле (папка modules): terraform init --plugin-dir=path/to/terraform.d или положить teraform.d в каждый модуль.


**Сценарий демо**
1) в variables (synapse-cluster) выставить машины, которые нужно заказать
по умолчанию default = "s6.2xlarge.4"

в скрипте main (synapse-cluster) выставить кол-во машин
по умолчанию count = 2

запустить тераформ
```
cd terraform/modules
terragrunt run-all apply
```

2) результатом выполнения скрипта teraform является kube_config, его необходимо скопировать в рабочию директорию и использовать в консольном клиенте
```
kubectl --kubeconfig=kube_config
```

3) создать проект с лейблом
```
kubectl --kubeconfig=kube_config create ns demo-second
kubectl --kubeconfig=kube_config label ns demo-second istio-injection=enabled
```

4a) настроить ui получив токен
```
kubectl get secret --kubeconfig=kube_config | grep cluster-admin-dashboard-sa
kubectl describe secret <secret_name> --kubeconfig=kube_config
```

4b) пробросить порты, чтобы ui был доступен локально (http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login):
```
kubectl proxy --kubeconfig=kube_config
```
87.242.89.135
4c) добавить в сервис аккаунт ссылку на pull secret
```
imagePullSecrets:
  - name: default-secret
  ```

5a) перейти в директорию yaml-configs и скопировать в нее kube_config

5b) развернуть sytester в default проект
```
kubectl --kubeconfig=kube_config apply -f sytester/configmaps-default.yml
kubectl --kubeconfig=kube_config apply -f sytester/secrets-default.yml
kubectl --kubeconfig=kube_config apply -f sytester/svc-default.yml
kubectl --kubeconfig=kube_config apply -f sytester/deployments-default.yml
```

6) зайти и сделать EIP для fronta sytester (сервис мастера не редактировать):
https://console.hc.sbercloud.ru/cce2.0/?agencyId=0b4d5ed38b0025a81febc01928a207b7&region=ru-moscow-1&locale=en-us#/app/resource/cluster/list

7) посмотреть EIP symple и kiali

8a) сделать конфиги ingress/egress в symple

пример шаблона:
```
ingress
demo-second
istio-system
create-find-order
8080
```

пример шаблона:
```
egress
demo-second
istio-system
8080
sytester-http-stub.default.svc.cluster.local
80
http
```

8b) включить логирование истио в config-map istio:
```
accessLogFile: /dev/stdout
```

8c) Добавить в деплоймент ingress, где указывается аннотация istio inject:
```
inject.istio.io/templates: gateway
prometheus.io/path: /stats/prometheus
prometheus.io/port: '15020'
prometheus.io/scrape: 'true'
```

9) установить ingress/egress
```
kubectl --kubeconfig=kube_config apply -f demo-project/ingress/ingress-http-patch-template.yml -n demo-second
kubectl --kubeconfig=kube_config apply -f demo-project/egress/egress-http-patch-template.yml -n demo-second
```

10) сделать eip для ingress

11) развернуть остальные приложения
```
kubectl --kubeconfig=kube_config apply -f demo-project/configmaps-demo.yml
kubectl --kubeconfig=kube_config apply -f demo-project/secrets-demo.yml
kubectl --kubeconfig=kube_config apply -f demo-project/other\ services/svc.yml
kubectl --kubeconfig=kube_config apply -f demo-project/other\ services/deployments.yml
```

12) сделать конфиг grpc-kafka в symple и установить + добавить http порт в Service

```
шаблон:
demo-second
поставщик
46.243.142.212:9094,45.9.27.224:9094,37.230.195.24:9094
SYNDEMO.NEWORDERCREATEDEVENT.V1

Добавить порт 8787 в Service:
- name: http
  port: 8787
  targetPort: 8787
```

```
kubectl --kubeconfig=kube_config apply -f demo-project/grpc-kafka/config.yml -n demo-second
kubectl --kubeconfig=kube_config apply -f demo-project/grpc-kafka/svc.yml  -n demo-second
kubectl --kubeconfig=kube_config apply -f demo-project/grpc-kafka/deployments.yml -n demo-second
```

13) установить сервис трансформации
```
kubectl --kubeconfig=kube_config apply -f demo-project/transform/svc.yml
kubectl --kubeconfig=kube_config apply -f demo-project/transform/configmap.yml
kubectl --kubeconfig=kube_config apply -f demo-project/transform/deployments.yml
```

14a) создать SE
```
kubectl --kubeconfig=kube_config apply -f serviceentry-mq.yml
kubectl --kubeconfig=kube_config apply -f serviceentry-db.yml
kubectl --kubeconfig=kube_config apply -f serviceentry-kafka.yml
```

14b) проверить все поды и если что починить
```
kubectl --kubeconfig=kube_config get pods -A
```

15a) запустить заглушки в UI Sytester

16) вызвать создание заказа:
```
curl http://87.242.89.135:8080/createOrder -H "content-type:application/json" -d "{\"customerId\":1,\"products\":[{\"id\":1}]}" -X POST
```

16b) вызвать получение записи
```
curl http://87.242.89.135:8080/getOrderById/4221"
```

17) посмотреть записи в базе
```

  37.230.195.8
  5432
  postgres

  postgres/qwe123
```

```
select * from "order" o  
order by id desc;
```

18) посмотреть в кафке (https://178.170.192.28:8080/demo User04)

19) содать два деплоймента с v1 и v2 и накатить канарейку (достаточно скопировать деплоймент grpc-kafka-adapter, изменить у него имя и версию лейбла с v1 на v2)
```
kubectl --kubeconfig=kube_config apply -f demo-project/canary/dr-canary.yml
kubectl --kubeconfig=kube_config apply -f demo-project/canary/vs-canary.yml
```

20) подать нагрузку 0.1 с помощью SyTester

21) установить аддон service-metrics, создать hpa и подать нагрузку

22) убедиться что создались новые поды

23) вывести кластер из эксплуатации
```
terragrunt run-all destroy
```


**Сборка образа**
1) импортировать корень проекта как gradle проект
2) собрать нужное приложение таской build
3) скопировать полученный образ в docker/<имя нужного сервиса>
4) перейти в данную директорию и запустить сборку образа сервиса или трансформации:

```
docker build . -t swr.ru-moscow-1.hc.sbercloud.ru/sber/template1:v1
```

```
docker build . -t swr.ru-moscow-1.hc.sbercloud.ru/sber/template1:v1
```
5) сохранить образ локально и залить его в реджестри через UI (https://console.hc.sbercloud.ru/swr/?agencyId=0b4d5ed38b0025a81febc01928a207b7&region=ru-moscow-1&locale=en-us#/app/dashboard)

```
docker save swr.ru-moscow-1.hc.sbercloud.ru/sber/template1:v1 > template1.tar
```
