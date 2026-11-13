"""Static YAML/Jinja checks with HA-like helpers; not a substitute for `ha core check`.
Run: python -m pip install PyYAML Jinja2; python test_templates.py
"""
import json
import math
from datetime import datetime, timedelta, timezone
from pathlib import Path
from types import SimpleNamespace
import yaml
from jinja2.nativetypes import NativeEnvironment

class Loader(yaml.SafeLoader):
    pass
Loader.add_constructor('!secret', lambda loader, node: loader.construct_scalar(node))
package = yaml.load(Path(__file__).with_name('kanucontrol_meter.yaml').read_text(), Loader=Loader)
env = NativeEnvironment()
now = datetime(2026, 11, 12, 12, 0, tzinfo=timezone.utc)
def is_number(value):
    try:
        return math.isfinite(float(value))
    except (ValueError, TypeError):
        return False
def as_timestamp(value, default=None):
    try:
        return value.timestamp()
    except (AttributeError, ValueError, TypeError):
        return default
env.globals.update(is_number=is_number, as_timestamp=as_timestamp, now=lambda: now, utcnow=lambda: now)
env.filters['to_json'] = json.dumps

def check_syntax(value):
    if isinstance(value, dict):
        for child in value.values(): check_syntax(child)
    elif isinstance(value, list):
        for child in value: check_syntax(child)
    elif isinstance(value, str) and ('{{' in value or '{%' in value):
        env.parse(value)
check_syntax(package)
automation = package['automation'][0]
assert automation['initial_state'] is False
assert automation['triggers'] == [{'trigger':'time_pattern','minutes':'0','seconds':'0'}]
assert package['rest_command']['kanucontrol_meter_import']['verify_ssl'] is True
snapshot = automation['actions'][0]['variables']['source_reading']
invalid = automation['actions'][1]['if'][0]['value_template']
stale = automation['actions'][2]['if'][0]['value_template']
payload = automation['actions'][3]['variables']['reading_payload']
class States:
    def __init__(self, value, age, missing=False):
        self.value, self.age, self.missing = value, age, missing
    def __getitem__(self, key):
        return None if self.missing else SimpleNamespace(last_reported=now-timedelta(seconds=self.age))
    def __call__(self, key):
        return self.value
cases = [('unknown',0,'kWh',False),('unavailable',0,'kWh',False),('nan',0,'kWh',False),
         ('inf',0,'kWh',False),('-1',0,'kWh',False),('1',301,'kWh',False),
         ('1',-10,'kWh',False),('1',0,'Wh',False),('1000000000000',0,'kWh',False),
         ('0',0,'kWh',True),('1234.56789',0,'kWh',True)]
for value, age, unit, expected in cases:
    context = dict(states=States(value,age), source_sensor='sensor.test',
                   state_attr=lambda entity, attribute: unit, max_sensor_age_seconds=300,
                   target_tenant='ekc_eschweilerkanuclub', target_meter_id=1)
    context['source_reading'] = env.from_string(snapshot).render(context)
    valid = not env.from_string(invalid).render(context) and not env.from_string(stale).render(context)
    assert valid == expected, (value, age, unit, valid)
    if valid:
        result = env.from_string(payload).render(context)
        if isinstance(result,str): result=json.loads(result)
        assert result['value'] == round(float(value),3)
        assert result['meterId'] == 1
        assert result['measuredAt'] == now.isoformat()
# Missing entity must not become zero.
context.update(states=States('unknown',0,True))
context['source_reading'] = env.from_string(snapshot).render(context)
assert env.from_string(invalid).render(context)
repeat = automation['actions'][4]['repeat']
terminal = repeat['until'][0]['value_template']
for status, attempt, done in [(0,1,False),(503,1,False),(429,1,False),(401,1,True),(409,1,True),(201,1,True),(200,1,True),(0,3,True)]:
    assert env.from_string(terminal).render(import_response={'status':status},repeat={'index':attempt}) == done
print('YAML, all template syntax, 12 sensor cases, payload and 8 retry cases passed.')
