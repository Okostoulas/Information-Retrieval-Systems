import numpy
import pandas


def get_two_dimensional_board_with_k_size(arr, k):
    board = numpy.empty((k, k))
    board.fill(0)

    counter = 0
    for row in range(0, k):
        for col in range(0, k):
            if row == col:
                board[row][col] = arr[counter]
                counter += 1

    return board


def check_if_all_queries_have_hits(q_size):
    count = 0
    for z in range(0, q_size):
        for x in q_index_transposed[z]:
            if x != 0:
                count += 1
                break

    if q_size == count:
        print("All queries have hits!")


'''
IMPORTANT STARTING DATA
'''
numpy.set_printoptions(formatter={"float": lambda x: ("%2.3f" % x)})
k = 300
index_file_path = 'D:\\Programming\\Python-Projects\\AI Stuff\\data.csv'
index_with_queries_file_path = 'D:\\Programming\\Python-Projects\\AI Stuff\\data_with_queries.csv'

# Read data.csv file into the A board
A = pandas.read_csv(index_file_path, sep=',', header=None).to_numpy()

print("Performing SVD analysis")
U, S, V = numpy.linalg.svd(A)

# Get S as a two dimensional array with it's contents on the diagonal line
two_dim_s = get_two_dimensional_board_with_k_size(S, k)

print('Calculating Ak board')
# Calculate Ak board
Uk = U[:, :k]
Sk = two_dim_s[:k, :k]
Vk = V[:k, :]
Ak = Uk.dot(Sk).dot(Vk)

# Read full vec array index, this one contains the queries in it too
full_vec_array = pandas.read_csv(index_with_queries_file_path, sep=',', header=None).to_numpy()

# Create q_index_transposed board
# We only need the Ak row length since, any more rows won't be in our dataset
q_index_rows = len(Ak)
q_index_cols = len(full_vec_array[0]) - len(Ak[0])
q_index = numpy.empty((q_index_rows, q_index_cols))
for i in range(0, q_index_rows):
    for j in range(0, q_index_cols):
        q_index[i][j] = full_vec_array[i][j + len(Ak[0]) - 1]

q_index_transposed = numpy.transpose(q_index)
q_size = len(q_index_transposed)
check_if_all_queries_have_hits(q_size)

print("Calculating q_k_ vectors board")
# Calculate q_k_vectors
q_k_vectors = numpy.empty((q_size, k))
for p in range(0, q_size):
    q_k_vectors[p] = q_index_transposed[p].dot(Uk).dot(Sk)

# Create similarity board based on the Frobenius norm
print("Creating similarity board")
sim = []
for q in q_k_vectors:
    sim.append(q.dot(Vk) / (numpy.linalg.norm(q)) * (numpy.linalg.norm(Vk)))

sim_arr = numpy.asarray(sim)

print("Saving similarity board and exiting")
# Save the similarity board
numpy.savetxt('q_k_similarity_vectors.csv', sim_arr, delimiter=',')
