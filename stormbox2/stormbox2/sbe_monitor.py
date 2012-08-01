"""
StormBoxEventMonitor
====
* Runs as system daemon via supervisord (supervisor config checked into repo
also)
* Periodically queries Storm production internal indexer for signup events
* Don't commit the internal indexer credentials to the repo (Retrieve them
from a system config file which  is not committed with the source)
* Launch StormBoxEventReceiver (fire & forget) with params
    type=signup
    email=users_email_address
* We can add different type events later such as type=upgrade etc

Requires
----
* a user in the internal indexer with User role: stormbox
* splunk-sdk: `pip install splunk-sdk`

Fake sbe_receiver
----

    #!/bin/bash
    echo $*

"""
import json
import logging
import os
import random
import subprocess
import time
import types

import splunklib.client


logging.basicConfig(
    format='%(asctime)s %(levelname)-8s %(message)s', filemode='w',
    level=logging.INFO)


class SplunkClient(object):
    """Class wrapper to splunklib.client.Service with more helper methods.
    NOTE: This may be replaced with http://eswiki.splunk.com/Project_Helmut
    """

    def __init__(self, **kwargs):
        """Creates a wrapper to the splunk.client.Service object"""
        for key in kwargs:
            if isinstance(kwargs[key], types.UnicodeType):
                kwargs[key] = kwargs[key].encode('ascii')
        self.service = splunklib.client.connect(**kwargs)
        logging.info('Connected to: %s://%s:%s' % (self.service.scheme,
                                                   self.service.host,
                                                   self.service.port))

    def search(self, search, within_seconds=60):
        """Search and return the result once the search is done

        Args:
            search: String containing splunk search command.
            within_seconds: an integer representing how many seconds it should
                wait for the job to be done.

        Returns:
            A list of results where each result is a dict
        """
        if not search.startswith('search') and not search.startswith('|'):
            search = ' '.join(['search', search])
        job = self.service.jobs.create(search)
        logging.info('Dispatching: %s' % search)
        start_time = time.time()
        while time.time() < start_time + within_seconds:
            job.refresh()
            if job['isDone'] == '1':
                break
            time.sleep(2)
            logging.info('Waiting for search to be done: %s seconds' % int(
                time.time() - start_time))
        else:
            logging.warn(
                "Search %s did not finish within %s secs, exiting anyway" % (
                    job.sid, within_seconds))
            job.finalize()
        results = job.results(output_mode='json').read()
        return [] if results == '' else json.loads(results)


def main():
    """Entry point"""

    # Get the settings
    settings_file = os.path.join(
        os.path.dirname(__file__), 'sbe_monitor_settings.json')
    logging.info("Settings from: %s" % os.path.abspath(settings_file))
    with open(settings_file, 'r') as f:
        settings = json.loads(f.read())
    logging.info(settings)

    # Connect to the magic box
    magic_box = SplunkClient(
        username=settings['username'], password=settings['password'],
        host=settings['host'], port=settings['port'])

    # Search for any result
    action = 'user_signup'
    earliest = time.time() - settings['period_seconds']
    search = (
        'search action=%s earliest=%s '
        '| stats count by email') % (action, earliest)
    results = magic_box.search(search)

    # Make sure they're not one of ours
    results = [r for r in results if not r['email'].endswith('splunk.com')]
    if len(results) <= 0:
        logging.info("It's been quiet lately - exiting")
        exit()
    for result in results:
        logging.info(result)

    # Select one by random and notify our insider
    lucky_bastard = random.choice(results)
    logging.info("Our lucky bastard: %s" % lucky_bastard)
    cmd = [
        'sbe_receiver',
        '--event_type', action,
        '--email', lucky_bastard['email']]
    returncode = subprocess.call(cmd)
    logging.info('%s returned: %s' % (' '.join(cmd), returncode))


if __name__ == "__main__":
    main()
