import json, sys, urllib, time

stdin = sys.stdin.read()
if stdin.strip() == '':
  sys.stderr.write("Empty input\n")
  sys.stderr.flush()
  sys.exit(0)

request_id = json.loads(stdin)["Requests"]["id"]

def process_task(task):
  status = task["Tasks"]["status"]
  if status in ["IN_PROGRESS", "PENDING", "QUEUED"]:
    sys.stderr.write("Task %s in progress: %s\n" % (task["Tasks"]["command_detail"], status))
    sys.stderr.flush()
    return False
  elif status in ["FAILED", "ABORTED", "TIMEDOUT"]:
    msg = "Task %s failed: %s\n" % (task["Tasks"]["command_detail"], task["Tasks"]["stderr"])
    sys.stderr.write(msg)
    sys.stderr.flush()
    raise Exception(msg)
  elif status == "COMPLETED":
    sys.stderr.write("Task %s completed.\n" % (task["Tasks"]["command_detail"]))
    sys.stderr.flush()
    return True
  else:
    msg = "Task %s is in unknown state: %s.\n" % (task["Tasks"]["command_detail"], status)
    sys.stderr.write(msg)
    sys.stderr.flush()
    raise Exception(msg)

while True:
  task_request = urllib.urlopen("http://admin:admin@sandbox-hdf.hortonworks.com:8080/api/v1/clusters/Sandbox/requests/%d?fields=tasks/Tasks/*" % request_id)
  tasks = json.load(task_request)["tasks"]
  if all(process_task(task) for task in tasks):
    sys.exit(0)
  time.sleep(5)
