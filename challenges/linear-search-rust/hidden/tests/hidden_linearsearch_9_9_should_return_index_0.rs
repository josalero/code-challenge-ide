use challenge::linear_search;

#[test]
fn hidden_linearsearch_9_9_should_return_index_0() {
    assert_eq!(linear_search(&[9], 9), 0);
}
