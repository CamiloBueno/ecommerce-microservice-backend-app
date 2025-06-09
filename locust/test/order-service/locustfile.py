from locust import HttpUser, task, between
import random

class OrderServiceUser(HttpUser):
    wait_time = between(1, 3)
    order_ids = [3,4]

    @task
    def getAllOrders(self):
        path = "/order-service/api/orders"
        with self.client.get(path, catch_response=True, name="/api/orders") as response:
            if response.status_code >= 200 and response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code} | Body: {response.text}")

    @task
    def getOrderById(self):
        order_id = random.choice(self.order_ids)
        path = f"/order-service/api/orders/{order_id}"
        with self.client.get(path, catch_response=True, name="/api/orders/{id}") as response:
            if response.status_code >= 200 and response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code} | Body: {response.text}")
'''
    @task
    def deleteOrderById(self):
        order_id = random.choice(self.order_ids)
        path = f"/order-service/api/orders/{order_id}"
        with self.client.delete(path, catch_response=True, name="/api/orders/{id} [DELETE]") as response:
            if response.status_code >= 200 and response.status_code < 300:
                response.success()
            else:
                response.failure(f"Failed to delete order {order_id}: {response.status_code} | {response.text}")
'''