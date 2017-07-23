# Pulls apart the pickled payload. This skips ahead 4 characters to safely ignore
# the header (length)
import cPickle
import struct
format = '!L'
headerLength = struct.calcsize(format)
payloadLength, = struct.unpack(format, payload[:headerLength])
batchLength = headerLength + payloadLength.intValue()
metrics = cPickle.loads(payload[headerLength:batchLength])
