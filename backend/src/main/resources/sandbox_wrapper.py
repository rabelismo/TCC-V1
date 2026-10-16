"""
Sandbox wrapper: executed BEFORE the student's code.
Restricts dangerous builtins and disables filesystem/network access.
"""
import builtins as _builtins
import sys as _sys

try:
    import resource as _resource
    _memory_bytes = int(_sys.argv[1]) * 1024 * 1024
    _cpu_seconds = int(_sys.argv[2])
    _resource.setrlimit(_resource.RLIMIT_AS, (_memory_bytes, _memory_bytes))
    _resource.setrlimit(_resource.RLIMIT_CPU, (_cpu_seconds, _cpu_seconds))
    del _resource, _memory_bytes, _cpu_seconds
except (ImportError, IndexError, ValueError):
    pass
_sys.argv[:] = _sys.argv[:1]

_ALLOWED_BUILTINS = {
    'abs', 'all', 'any', 'ascii', 'bin', 'bool', 'bytearray', 'bytes',
    'callable', 'chr', 'complex', 'dict', 'dir', 'divmod', 'enumerate',
    'filter', 'float', 'format', 'frozenset', 'hash', 'hex', 'id',
    'input', 'int', 'isinstance', 'issubclass', 'iter', 'len', 'list',
    'map', 'max', 'min', 'next', 'object', 'oct', 'ord', 'pow',
    'print', 'range', 'repr', 'reversed', 'round', 'set', 'slice',
    'sorted', 'str', 'sum', 'tuple', 'type', 'zip',
    'True', 'False', 'None',
    'ValueError', 'TypeError', 'IndexError', 'KeyError', 'ZeroDivisionError',
    'RuntimeError', 'StopIteration', 'Exception', 'ArithmeticError',
    'OverflowError', 'AttributeError', 'NameError', 'NotImplementedError',
}

_delattr = delattr
for _name in list(vars(_builtins)):
    if _name not in _ALLOWED_BUILTINS:
        try:
            _delattr(_builtins, _name)
        except (AttributeError, TypeError):
            pass
del _delattr

_sys.modules['os'] = None
_sys.modules['subprocess'] = None
_sys.modules['shutil'] = None
_sys.modules['socket'] = None
_sys.modules['http'] = None
_sys.modules['http.client'] = None
_sys.modules['http.server'] = None
_sys.modules['urllib'] = None
_sys.modules['urllib.request'] = None
_sys.modules['requests'] = None
_sys.modules['ctypes'] = None
_sys.modules['signal'] = None
_sys.modules['multiprocessing'] = None
_sys.modules['pickle'] = None
_sys.modules['shelve'] = None
_sys.modules['importlib'] = None
_sys.modules['code'] = None
_sys.modules['codeop'] = None
_sys.modules['webbrowser'] = None
_sys.modules['pathlib'] = None
_sys.modules['tempfile'] = None
_sys.modules['glob'] = None
_sys.modules['fnmatch'] = None

del _builtins, _sys, _name, _ALLOWED_BUILTINS
