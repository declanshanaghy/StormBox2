"""
StormBoxEventMonitor
====================
    Runs as system daemon via supervisord (supervisor config checked into repo also)
    Periodically queries Storm production internal indexer for signup events
        Don't commit the internal indexer credentials to the repo (Retrieve them from a system config file which  is not committed with the source)
    Launch StormBoxEventReceiver (fire & forget) with params
        type=signup
        email=users_email_address
    We can add different type events later such as type=upgrade etc

Requires
--------
* user in internal indexer with User role: stormbox/stormbox
* splunk-sdk: pip install splunk-sdk

"""
import json
import logging
import time
import types

import splunklib.client


SUBDOMAIN = 'staging.'
INTERNAL_INDEXER_HOST = 'splunk.%ssplunkstorm.com' % SUBDOMAIN
INTERNAL_INDEXER_USERNAME = 'stormbox'
INTERNAL_INDEXER_PASSWORD = 'stormbox'


class SplunkClient(object):
    """Class wrapper to splunklib.client.Service with more helper methods.
    NOTE: This may be replaced with http://eswiki.splunk.com/Project_Helmut
    """

    def __init__(self, **kwargs):
        self.service = splunklib.client.connect(**kwargs)
        logging.info('Connected to: %s://%s:%s' % (self.service.scheme,
                                                   self.service.host,
                                                   self.service.port))

    def search(self, search, within_seconds=60, output_mode='json'):
        """Search and return the result stream once the search is done

        Args:
            search: String containing splunk search command.
            within_seconds: an integer representing how many seconds it should
                wait for the job to be done.
            kwargs: Same keyword arguments arguments you pass to dispatch a
                search using the splunk SDK.

        Returns:
            A stream of search results
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
            logging.info('Waiting for search to be done: %s seconds' %
                          int(time.time() - start_time))
        else:
            logging.warn(
                "Search %s did not finish within %s secs, exiting anyway" % (
                    job.sid, within_seconds))
            job.finalize()
        stream = job.results(output_mode=output_mode)
        return json.loads(stream.read())


def main():
    internal_indexer = SplunkClient(username=INTERNAL_INDEXER_USERNAME,
                                    password=INTERNAL_INDEXER_PASSWORD,
                                    host=INTERNAL_INDEXER_HOST)
    action = 'user_signup'
    results = internal_indexer.search('search action=%s | stats count by email' % action)
    if len(results) <= 0:
        exit()

if __name__ == "__main__":
    main()
