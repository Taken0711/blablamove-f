#!/usr/bin/env python3

import requests
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("--skip-externals", help="skip external service tests", action="store_true")
args = parser.parse_args()

DEFAULT = "\033[00m"
RED = "\033[91m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
MAGENTA = "\035[95m"
CYAN = "\033[96m"

rpc_id = 0

def rpc_call(url, method, params):
    global rpc_id
    rpc_id += 1
    res = requests.post(url, json={"jsonrpc": "2.0", "method": method, "id": rpc_id, "params": params})
    response_object = res.json()
    return response_object.get("result", None), response_object.get("error", None), res.status_code

def request_webservice(url, method, params):
    res, err, status = rpc_call(url, method, params)
    if err:
        print("\tError %d: %s" % (status, err))
        exit(status)
    print("\tResponse: %s" % res)
    return res

def print_color(text, color): print(color, text, DEFAULT)

def step(title): print_color("#=== %s ===#" % title, BLUE)

def skipped():
    print("\tResult:", end="")
    print_color("SKIPPED", YELLOW)
    print()

def assert_equals(expected, actual):
    res = expected == actual
    print("\tResult:", end="")
    if res:
        print_color("OK", GREEN)
        print()
    else:
        print_color("ERROR", RED)
        print("\tExpected:", expected)
        print("\tActual:  ", actual)
        exit(1)

step("Johann notify a car crash")
assert_equals(True, request_webservice("http://localhost:8080/incident", "notifyCarCrash", {"username": "Johann", "latitude": 10, "longitude": 25}))

step("Johann's insurance has been notified")
if args.skip_externals:
    skipped()
else:
    assert_equals(True, requests.get("http://localhost:5000/insurances/Johann").json().get("requestedInsurance", None))

step("Johann is notified that Erick will take the packages")
assert_equals(2, len(request_webservice("http://localhost:8080/notification", "pullNotificationForUser", {"username": "Johann"})))

step("Erick is notified that he will take Johann's packages")
assert_equals(2, len(request_webservice("http://localhost:8080/notification", "pullNotificationForUser", {"username": "Erick"})))

step("Jeremy is notified that Johann had an accident and that Erick will take his package")
assert_equals(2, len(request_webservice("http://localhost:8080/notification", "pullNotificationForUser", {"username": "Jeremy"})))

step("Thomas is notified that Johann had an accident and that Erick will take his package")
assert_equals(2, len(request_webservice("http://localhost:8080/notification", "pullNotificationForUser", {"username": "Thomas"})))

if not args.skip_externals:
    nb_points_before = requests.get("http://localhost:5001/users/Erick").json().get("points", None)

step("The package is dropped")
assert_equals(True, request_webservice("http://localhost:8080/drop", "computePoints", {"mission": 8}))

if args.skip_externals:
    skipped()
else:
    nb_points_after = requests.get("http://localhost:5001/users/Erick").json().get("points", None)
    assert_equals(True, nb_points_after > nb_points_before)