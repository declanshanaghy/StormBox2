"""setup.py script for the Input Backup Service."""

import setuptools


setuptools.setup(
    name='stormbox2',
    version='1',
    packages=setuptools.find_packages(exclude=['tests']),
    install_requires=[],
    entry_points={
        'console_scripts': [
            ]
        },
    test_suite='tests',
    author='Splunk Storm Engineering Team',
    author_email='cloud-eng@splunk.com',
    description='Splunk Storm Announcement System',
)
