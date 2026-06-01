use challenge::contains_duplicate;

#[test]
fn public_containsduplicate_1_2_3_4_should_be_false() {
    assert_eq!(contains_duplicate(&[1, 2, 3, 4]), false);
}
