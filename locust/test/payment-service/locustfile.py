from locust import HttpUser, task, between
import random

class PaymentServiceUser(HttpUser):
    wait_time = between(1, 3)
    payment_ids = [1, 2, 3]
'''
    @task
    def getAllPayments(self):
        path = "/payment-service/api/payments"
        with self.client.get(path, catch_response=True, name="/api/payments") as response:
            if response.status_code >= 200 and response.status_code < 300:
                response.success()
                print(f"Response: {response.text}")
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
'''
    @task
    def getPaymentById(self):
        payment_id = random.choice(self.payment_ids)
        path = f"/payment-service/api/payments/4"
        with self.client.get(path, catch_response=True, name="/api/payments/{id}") as response:
            if response.status_code >= 200 and response.status_code < 300:
                    response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")